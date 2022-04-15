package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.ChangeGroupStatusListRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupListRequest;
import ru.maruchekas.keycloak.api.request.DeleteGroupRequest;
import ru.maruchekas.keycloak.api.request.EditGroupListRequest;
import ru.maruchekas.keycloak.service.GroupService;
import ru.maruchekas.keycloak.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "Контроллер для работы с группами")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Метод получения списка групп")
    public ResponseEntity<?> getAllGroups(@RequestHeader("Authorization") String accessToken) {
        log.info("Попытка получения списка групп");
        return ResponseEntity.ok(groupService.getAllGroups(accessToken));

    }

    @PostMapping("/{group-id}")
    @Operation(summary = "Метод получения группы по id")
    public ResponseEntity<?> getGroupById(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable("group-id") String groupId) {
        log.info("Попытка получения группы \"{}\"", groupId);
        return ResponseEntity.ok(groupService.getGroupById(accessToken, groupId));

    }

    @PostMapping("add-group")
    @Operation(summary = "Метод создания новой группы")
    public ResponseEntity<?> postNewGroup(@RequestHeader("Authorization") String accessToken,
                                          @RequestBody CreateGroupListRequest createGroupRequest) {
        log.info("Попытка создания новой группы");
        return ResponseEntity.ok(groupService.createGroup(createGroupRequest, accessToken));
    }

    @DeleteMapping("")
    @Operation(summary = "Метод удаления группы по id")
    public ResponseEntity<?> deleteGroupById(@RequestHeader("Authorization") String accessToken,
                                             @RequestBody DeleteGroupRequest deleteRequest) {
        log.info("Попытка удаления группы");
        return ResponseEntity.ok(groupService.deleteGroupById(accessToken, deleteRequest));

    }

    @PutMapping("/edit-group")
    @Operation(summary = "Метод изменения группы по id")
    public ResponseEntity<?> updateGroupById(@RequestHeader("Authorization") String accessToken,
                                             @RequestBody EditGroupListRequest request) {
        log.info("Попытка изменения группы ");
        return ResponseEntity.ok(groupService.updateGroup(accessToken, request));

    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Метод получения списка пользователей для группы")
    public ResponseEntity<?> getMembersByGroupId(@RequestHeader("Authorization") String accessToken,
                                                 @PathVariable("id") String id){
        log.info("Попытка получения списка участников группы \"{}\"", id);
        return ResponseEntity.ok(groupService.getGroupMembersByGroupId(accessToken, id));
    }

    @PutMapping( "/block")
    @Operation(summary = "Метод блокировки группы или списка групп")
    public ResponseEntity<?> blockGroup(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody ChangeGroupStatusListRequest changeGroupStatusRequest){
        List<String> ids = new ArrayList<>();
        changeGroupStatusRequest.getGroupStatusChangeRequestList().forEach(group -> ids.add(group.getGroupId()));
        log.info("Попытка блокировки групп(ы) {}", ids);
        return ResponseEntity.ok(groupService.changeBlockStatusGroup(accessToken, changeGroupStatusRequest));
    }

    @PutMapping( "/unblock")
    @Operation(summary = "Метод разблокировки группы или списка групп")
    public ResponseEntity<?> unblockGroup(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody ChangeGroupStatusListRequest changeGroupStatusRequest){
        List<String> ids = new ArrayList<>();
        changeGroupStatusRequest.getGroupStatusChangeRequestList().forEach(group -> ids.add(group.getGroupId()));
        log.info("Попытка разблокировки групп(ы) {}", ids);
        return ResponseEntity.ok(groupService.changeBlockStatusGroup(accessToken, changeGroupStatusRequest));
    }

    @DeleteMapping("/{groupId}/role/{roleId}")
    @Operation(summary = "Метод добавления роли в группу")
    public ResponseEntity<?> addRoleToGroup(@RequestHeader("Authorization") String accessToken,
                                               @PathVariable String groupId,
                                               @PathVariable("roleId") String roleId){
        return ResponseEntity.ok(groupService.deleteRoleFromGroup(groupId, roleId, accessToken));
    }

}
