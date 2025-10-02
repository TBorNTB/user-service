package com.sejong.userservice.core.user;

import java.util.List;

public interface UserRoleRepository {

    RoleChange save(RoleChange roleChange);

    void updateAccept(Long roleChangeId, String adminUsername);

    void updateReject(Long roleChangeId, String adminUsername);

    List<RoleChange> findAllAboutStatusIsPending();
}
