# 알람 도메인 추천 사항

## 현재 구현

- **알람 API 페이지네이션**: `GET /alarm/received/page?page=0&size=20&alarmType=...&seen=...`
  - `alarmType`: 생략 시 전체 타입
  - `seen`: 생략 시 전체, `true` 읽은 알람만, `false` 안 읽은 알람만
  - 정렬: `createdAt` 내림차순(최신순)

- **기존 API**: `GET /alarm/received?alarmType=...` (미확인만, 호환용 유지)

---

## 도메인 특성 요약

| 구분 | 내용 |
|------|------|
| **서비스** | 사용자·콘텐츠 중심 (user-service) |
| **도메인 타입** | NEWS, PROJECT, QNA, DOCUMENT, ARTICLE, GLOBAL |
| **포스트 타입** | NEWS, PROJECT, QNA_QUESTION, QNA_ANSWER, DOCUMENT, ARTICLE |
| **기능** | 댓글(대댓글), 좋아요, 채팅, 회원/역할(ADMIN, SENIOR, FULL_MEMBER 등) |

---

## 현재 알람 타입 (AlarmType)

| 타입 | 설명 |
|------|------|
| `COMMENT_ADDED` | 내 글에 댓글 |
| `COMMENT_REPLY_ADDED` | 내 댓글에 대댓글 |
| `POST_LIKED` | 내 글 좋아요 |
| `SIGNUP` | 새 회원 가입 (관리자용) |

---

## 도메인 특성 반영 추천 알람 타입

### 1. 우선 추천 (구현 난이도·효과 고려)

| 추천 타입 | 설명 | 발행 시점 |
|-----------|------|-----------|
| **MENTION** | @멘션 알림 | 댓글/게시글에서 나를 @멘션했을 때 |
| **QNA_ANSWERED** | QnA 답변 알림 | 내 QnA 질문에 답변이 달렸을 때 (DomainType.QNA) |
| **PROJECT_INVITED** | 프로젝트 초대 | 프로젝트에 초대되었을 때 (DomainType.PROJECT) |
| **SYSTEM_ANNOUNCEMENT** | 공지/시스템 알림 | 관리자 공지, 정책 변경 등 (owner=전체 또는 역할별) |

### 2. 선택 추천 (기능 존재 시)

| 추천 타입 | 설명 | 전제 조건 |
|-----------|------|-----------|
| **ROLE_CHANGED** | 권한 변경 | ADMIN이 멤버 등급 변경 시 |
| **DOCUMENT_SHARED** | 다큐먼트 공유 | 다큐먼트가 나에게 공유되었을 때 |
| **CHAT_MESSAGE** | 채팅 메시지 | 채팅 도메인 연동 시 (방 참여자에게) |
| **POST_COMMENTED** | 내가 쓴 글에 댓글 | 현재 COMMENT_ADDED와 유사하나, “내 글” 강조 시 구분용 |

### 3. 확장 시 고려

- **LIKE_ON_COMMENT**: 댓글 좋아요 기능 도입 시
- **FOLLOW / NEW_FOLLOWER**: 팔로우 기능 도입 시
- **ARTICLE_PUBLISHED**: 아티클 게시/승인 시 (편집 워크플로우 있을 경우)

---

## 적용 시 참고

1. **AlarmType enum**  
   위 타입 중 채택한 것만 `AlarmType`에 추가.

2. **메시지 문구**  
   `Alarm.buildMessage()`의 `switch`에 타입별 메시지 추가 (예: `QNA_ANSWERED` → "OO님이 QnA에 답변을 남겼습니다.").

3. **이벤트 발행**  
   해당 액션이 일어나는 서비스(Comment, Like, User, Project, QnA 등)에서 `DomainAlarmEvent` 발행 후 Kafka/알람 consumer에서 저장.

4. **필터/설정**  
   클라이언트에서 알람 타입별 필터(이미 `alarmType` 파라미터로 지원) 및 “알람 끄기” 설정 시 위 타입을 단위로 사용하면 됨.
