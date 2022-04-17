package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.*;
import ru.maruchekas.keycloak.service.GroupService;

@Slf4j
@Tag(name = "Контроллер для работы с группами")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Метод получения списка групп")
    public ResponseEntity<?> getFilteredGroups(@RequestHeader("Authorization") String accessToken,
                                               @RequestBody FilterRequest filter) {
        log.info("Попытка получения списка групп");
        return ResponseEntity.ok(groupService.getGroupsByQuery(filter, accessToken));

    }

    @PostMapping("/{group-id}")
    @Operation(summary = "Метод получения группы по id")
    public ResponseEntity<?> getGroupById(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable("group-id") String groupId) {
        log.info("Попытка получения группы \"{}\"", groupId);
        return ResponseEntity.ok(groupService.getGroupById(groupId, accessToken));

    }

    @PostMapping("add-group")
    @Operation(summary = "Метод создания новой группы или списка групп")
    public ResponseEntity<?> postNewGroup(@RequestHeader("Authorization") String accessToken,
                                          @RequestBody CreateGroupListRequest createGroupRequest) {
        log.info("Попытка создания новой группы");
        return ResponseEntity.ok(groupService.createGroup(createGroupRequest, accessToken));
    }

    @DeleteMapping("")
    @Operation(summary = "Метод удаления группы или списка групп")
    public ResponseEntity<?> deleteGroupById(@RequestHeader("Authorization") String accessToken,
                                             @RequestBody DeleteGroupRequest deleteRequest) {
        log.info("Попытка удаления группы");
        return ResponseEntity.ok(groupService.deleteGroupById(deleteRequest, accessToken));

    }

    @PutMapping("/edit-group")
    @Operation(summary = "Метод изменения группы или списка групп")
    public ResponseEntity<?> editGroups(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody EditGroupListRequest request) {
        log.info("Попытка изменения группы ");
        return ResponseEntity.ok(groupService.editListGroup(request, accessToken));

    }

    @PostMapping( "/block")
    @Operation(summary = "Метод блокировки группы или списка групп")
    public ResponseEntity<?> blockGroup(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody GroupStatusChangeRequest changeStatusRequest){
        log.info("Попытка блокировки групп(ы)");
        return ResponseEntity.ok(groupService.changeBlockStatusGroup(changeStatusRequest, accessToken));
    }

    @PostMapping( "/unblock")
    @Operation(summary = "Метод разблокировки группы или списка групп")
    public ResponseEntity<?> unblockGroup(@RequestHeader("Authorization") String accessToken,
                                        @RequestBody GroupStatusChangeRequest changeStatusRequest){
        log.info("Попытка разблокировки групп(ы)");
        return ResponseEntity.ok(groupService.changeBlockStatusGroup(changeStatusRequest, accessToken));
    }

}