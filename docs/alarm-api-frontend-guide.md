# 알람(Alarm) API · 프론트엔드 연동 가이드

프론트에서 알람 기능을 구현할 때 참고할 API 명세와 연동·UI 가이드입니다.

---

## 1. 공통 사항

### Base URL
- 알람 API 경로: **`/alarm`** (서비스 베이스 URL 뒤에 붙입니다.)

### 인증
- **모든 API**는 로그인 필요 (Bearer Token).
- 요청 헤더: `Authorization: Bearer {accessToken}`

### 공통 에러
- `401`: 미로그인 또는 토큰 만료 → 로그인 유도
- `403`: 본인 알람이 아닌 경우 (읽음/삭제 시)
- `404`: 알람 ID 없음 (단건 조회/수정/삭제 시)

---

## 2. 데이터 타입 정의 (TypeScript 예시)

```ts
// 알람 타입 (댓글, 대댓글, 좋아요, 가입 등)
type AlarmType = 'COMMENT_ADDED' | 'COMMENT_REPLY_ADDED' | 'POST_LIKED' | 'SIGNUP';

// 알람이 발생한 도메인 (표시용 라벨에 활용)
type DomainType = 'NEWS' | 'PROJECT' | 'QNA' | 'DOCUMENT' | 'ARTICLE' | 'GLOBAL';

interface Alarm {
  id: number;
  alarmType: AlarmType;
  domainType: DomainType;
  domainId: number;
  actorUsername: string;   // 알람을 발생시킨 사용자
  ownerUsername: string;   // 알람을 받는 사용자 (현재 로그인 유저)
  message: string;         // 화면에 그대로 쓸 수 있는 문구 (예: "홍길동님이 뉴스에 댓글을 남겼습니다.")
  seen: boolean;           // 읽음 여부
  createdAt: string;       // ISO 8601 (예: "2025-02-22T14:30:00")
}

// 페이지네이션 응답 (목록 조회 시)
interface AlarmListResponse {
  message: string;
  size: number;
  page: number;
  totalPage: number;
  data: Alarm[];
}

// 미확인 개수 응답
interface AlarmUnreadCountResponse {
  count: number;
}

// 일괄 요청 Body (읽음 일괄 / 삭제 일괄)
interface AlarmBulkRequest {
  alarmIds: number[];  // 비어 있으면 400
}
```

---

## 3. API 목록 요약

| 용도 | 메서드 | 경로 | 비고 |
|------|--------|------|------|
| **목록** (페이지네이션) | GET | `/alarm/received/page` | 권장. page, size, 필터 지원 |
| 목록 (미확인만, 레거시) | GET | `/alarm/received` | alarmType 필수 |
| **미확인 개수** | GET | `/alarm/unread-count` | 뱃지용 |
| 읽음 (단건) | POST | `/alarm/{alarmId}/seen` | |
| **읽음 (일괄)** | POST | `/alarm/seen/bulk` | Body: `{ alarmIds: [] }` |
| **읽음 (전체)** | POST | `/alarm/seen/all` | 미확인 전체 읽음 |
| **삭제 (단건)** | DELETE | `/alarm/{alarmId}` | |
| **삭제 (일괄)** | DELETE | `/alarm/bulk` | Body: `{ alarmIds: [] }` |
| **삭제 (읽은 것 전체)** | DELETE | `/alarm/read` | 읽은 알람만 일괄 삭제 |

---

## 4. API 상세 스펙

### 4.1 알람 목록 (페이지네이션) — **목록 화면용 권장**

```
GET /alarm/received/page
```

**Query (모두 선택)**

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | number | 0 | 페이지 번호 (0부터) |
| size | number | 6 | 한 페이지 개수 (1~100) |
| alarmType | string | (없음) | 필터: COMMENT_ADDED, COMMENT_REPLY_ADDED, POST_LIKED, SIGNUP. 없으면 전체 |
| seen | boolean | (없음) | true=읽은 것만, false=안 읽은 것만. 없으면 전체 |

**예시**
- 전체 최신순 1페이지: `GET /alarm/received/page?page=0&size=20`
- 안 읽은 것만: `GET /alarm/received/page?seen=false&page=0&size=20`
- 댓글 알람만: `GET /alarm/received/page?alarmType=COMMENT_ADDED&page=0&size=20`

**응답** `200 OK`
```json
{
  "message": "조회 성공",
  "size": 20,
  "page": 0,
  "totalPage": 3,
  "data": [
    {
      "id": 101,
      "alarmType": "COMMENT_ADDED",
      "domainType": "NEWS",
      "domainId": 5,
      "actorUsername": "user1",
      "ownerUsername": "me",
      "message": "홍길동님이 뉴스에 댓글을 남겼습니다.",
      "seen": false,
      "createdAt": "2025-02-22T14:30:00"
    }
  ]
}
```
- 목록은 **항상 `createdAt` 기준 최신순**입니다.

---

### 4.2 미확인 알람 개수 — **뱃지/알람 아이콘 숫자**

```
GET /alarm/unread-count
```

**응답** `200 OK`
```json
{
  "count": 7
}
```
- 로그인 후 헤더/알람 아이콘 옆 뱃지에 `count` 표시하면 됩니다.
- 알람 목록에서 읽음 처리·삭제 후 이 API를 다시 호출해 뱃지를 갱신할 수 있습니다.

---

### 4.3 알람 단건 읽음 처리

```
POST /alarm/{alarmId}/seen
```
- Path: `alarmId` — 알람 ID  
- Body 없음  

**응답** `200 OK` (Body 없음)  
- 실패: 본인 알람이 아니면 `403`, 없으면 `404`

---

### 4.4 알람 일괄 읽음 처리

```
POST /alarm/seen/bulk
Content-Type: application/json
```
**Body**
```json
{
  "alarmIds": [1, 2, 3]
}
```
- `alarmIds`는 비어 있으면 안 됩니다 (400).
- 요청한 ID 중 **본인 알람만** 읽음 처리됩니다. 타인 알람 ID는 무시됩니다.

**응답** `200 OK` (Body 없음)

---

### 4.5 전체 알람 읽음 처리

```
POST /alarm/seen/all
```
- Body 없음.  
- **미확인(seen=false) 알람 전체**를 읽음 처리합니다.

**응답** `200 OK` (Body 없음)

---

### 4.6 알람 단건 삭제

```
DELETE /alarm/{alarmId}
```
- Path: `alarmId` — 알람 ID  

**응답** `204 No Content`  
- 본인 알람이 아니면 `403`, 없으면 `404`

---

### 4.7 알람 일괄 삭제

```
DELETE /alarm/bulk
Content-Type: application/json
```
**Body**
```json
{
  "alarmIds": [1, 2, 3]
}
```
- `alarmIds` 비어 있으면 400.
- 요청한 ID 중 **본인 알람만** 삭제됩니다.

**응답** `204 No Content`

---

### 4.8 읽은 알람 전체 삭제

```
DELETE /alarm/read
```
- **이미 읽음(seen=true)인 알람만** 일괄 삭제합니다.  
- Body 없음.

**응답** `204 No Content`

---

## 5. 화면별 연동 시나리오

### 5.1 헤더 알람 아이콘 + 뱃지
1. 로그인 후: `GET /alarm/unread-count` → `count`를 뱃지 숫자로 표시.
2. 주기적 갱신(폴링) 또는 알람 목록/읽음·삭제 후 한 번 더 호출해 뱃지 갱신.

### 5.2 알람 목록 페이지 (드롭다운 또는 전체 페이지)
1. **최초 로드**: `GET /alarm/received/page?page=0&size=20` (필요 시 `seen=false` 등 추가).
2. **더 보기/페이지네이션**: `page=1`, `page=2` … 동일 쿼리로 요청.
3. **필터**: 탭/필터가 있으면 `alarmType`, `seen` 쿼리로 전달.
4. **아이템 클릭 시**: 해당 글로 이동 + `POST /alarm/{alarmId}/seen` 호출 후 목록/뱃지 갱신.

### 5.3 “모두 읽음” 버튼
- `POST /alarm/seen/all` 호출 후, 목록 다시 조회 또는 `seen=false` 필터 제거 후 재요청 + `GET /alarm/unread-count`로 뱃지 갱신.

### 5.4 “선택 읽음” (체크박스 여러 개)
- 선택한 ID 배열로 `POST /alarm/seen/bulk` Body: `{ "alarmIds": [ ... ] }` 호출 후 목록·뱃지 갱신.

### 5.5 알람 하나 삭제 (휴지통/삭제 버튼)
- `DELETE /alarm/{alarmId}` 호출 후 해당 항목을 목록에서 제거하거나 목록 API 재요청.

### 5.6 “선택 삭제”
- `DELETE /alarm/bulk` Body: `{ "alarmIds": [ ... ] }` 호출 후 목록에서 제거 또는 목록 재요청.

### 5.7 “읽은 알람 모두 삭제”
- `DELETE /alarm/read` 호출 후 목록 재요청(또는 읽은 항목만 로컬에서 제거).

---

## 6. UI 구현 시 참고

- **message**: 서버에서 만든 문구이므로 그대로 표시해도 됩니다. (예: "홍길동님이 뉴스에 댓글을 남겼습니다.")
- **알람 클릭 시 이동**: `domainType` + `domainId`로 해당 콘텐츠 상세 페이지로 이동하면 됩니다. (라우팅 규칙은 프론트/백 협의.)
- **seen**: `false`면 읽지 않은 알람이므로 강조(배경색, 굵은 글씨 등)로 구분할 수 있습니다.
- **alarmType / domainType**: 아이콘 또는 라벨 분기(댓글/좋아요/가입 등)에 사용할 수 있습니다.
- **createdAt**: "방금 전", "n분 전", "n시간 전" 등 상대 시간으로 표시하면 좋습니다.

---

## 7. 에러 응답 형식 (참고)

서비스 공통 에러 형식을 따릅니다. 예시:
```json
{
  "result": null,
  "resultCode": "ALARM_NOT_FOUND",
  "resultMessage": "알람을 찾을 수 없습니다."
}
```
- `ALARM_ACCESS_DENIED`: 본인 알람이 아님 (403)  
- `ALARM_NOT_FOUND`: 알람 없음 (404)  
- Body 검증 실패(빈 `alarmIds` 등): 400

---

이 문서만으로 알람 API 연동 및 알람 목록/뱃지/읽음·삭제 UI를 구현할 수 있습니다.  
추가로 Swagger(OpenAPI)가 있다면 `/alarm` 경로에서 위 API를 직접 호출해 볼 수 있습니다.
