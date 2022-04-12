package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.ChangeGroupStatusListRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.service.GroupService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "Контроллер для работы с группами")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @Operation(summary = "Метод получения списка групп")
    public ResponseEntity<?> getAllGroups(@RequestHeader("Authorization") String accessToken) {
        log.info("Попытка получения списка групп");
        return ResponseEntity.ok(groupService.getAllGroups(accessToken));

    }

    @GetMapping("/{id}")
    @Operation(summary = "Метод получения группы по id")
    public ResponseEntity<?> getGroupById(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable("id") String id) {
        log.info("Попытка получения группы \"{}\"", id);
        return ResponseEntity.ok(groupService.getGroupById(accessToken, id));

    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Метод создания новой группы")
    public ResponseEntity<?> postNewGroup(@RequestHeader("Authorization") String accessToken,
                                          @RequestBody CreateGroupRequest createGroupRequest) {
        log.info("Попытка создания новой группы группы с именем \"{}\"", createGroupRequest.getName());
        return ResponseEntity.ok(groupService.createGroup(createGroupRequest, accessToken));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Метод удаления группы по id")
    public ResponseEntity<?> deleteGroupById(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id) {
        log.info("Попытка удаления группы \"{}\"", id);
        return ResponseEntity.ok(groupService.deleteGroupById(accessToken, id));

    }

    @PutMapping("/{id}")
    @Operation(summary = "Метод изменения группы по id")
    public ResponseEntity<?> updateGroupById(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id,
                                             @RequestBody CreateGroupRequest request) {
        log.info("Попытка изменения группы \"{}\"", id);
        return ResponseEntity.ok(groupService.updateGroupById(accessToken, id, request));

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

    @GetMapping("/{id}/roles")
    @Operation(summary = "Метод получения списка ролей для группы")
    public ResponseEntity<?> getRolesByGroupId(@RequestHeader("Authorization") String accessToken,
                                               @PathVariable("id") String id){
        log.info("Попытка получения ролей для групп {}", id);
        return ResponseEntity.ok(groupService.getRoles(accessToken, id));
    }

}
