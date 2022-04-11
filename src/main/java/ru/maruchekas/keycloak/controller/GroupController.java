package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.ChangeGroupStatusListRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.service.GroupService;

@Tag(name = "Контроллер для работы с группами")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @Operation(summary = "Метод получения списка групп")
    public ResponseEntity<?> getAllGroups(@RequestHeader("Authorization") String accessToken) {
        return ResponseEntity.ok(groupService.getAllGroups(accessToken));

    }

    @GetMapping("/{id}")
    @Operation(summary = "Метод получения группы по id")
    public ResponseEntity<?> getGroupById(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable("id") String id) {
        return ResponseEntity.ok(groupService.getGroupById(accessToken, id));

    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Метод создания новой группы")
    public ResponseEntity<?> postNewGroup(@RequestHeader("Authorization") String accessToken,
                                          @RequestBody CreateGroupRequest createGroupRequest) {
        return ResponseEntity.ok(groupService.createGroup(createGroupRequest, accessToken));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Метод удаления группы по id")
    public ResponseEntity<?> deleteGroupById(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id) {
        return ResponseEntity.ok(groupService.deleteGroupById(accessToken, id));

    }

    @PutMapping("/{id}")
    @Operation(summary = "Метод изменения группы по id")
    public ResponseEntity<?> updateGroupById(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id,
                                             @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroupById(accessToken, id, request));

    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Метод получения списка пользователей для группы")
    public ResponseEntity<?> getMembersByGroupId(@RequestHeader("Authorization") String accessToken,
                                                 @PathVariable("id") String id){
        return ResponseEntity.ok(groupService.getGroupMembersByGroupId(accessToken, id));
    }

    @PutMapping(value = "/block")
    @Operation(summary = "Метод блокировки группы или списка групп")
    public ResponseEntity<?> blockGroup(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody ChangeGroupStatusListRequest changeGroupStatusRequest){
        return ResponseEntity.ok(groupService.changeBlockStatusGroup(accessToken, changeGroupStatusRequest));
    }

    @PutMapping(value = "/unblock")
    @Operation(summary = "Метод разблокировки группы или списка групп")
    public ResponseEntity<?> unblockGroup(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody ChangeGroupStatusListRequest changeGroupStatusRequest){
        return ResponseEntity.ok(groupService.changeBlockStatusGroup(accessToken, changeGroupStatusRequest));
    }

}
