package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createGroup(@RequestHeader("Authorization") String accessToken,
                                         @RequestBody CreateGroupRequest createGroupRequest){
        return ResponseEntity.ok(groupService.createGroupThroughRepresentation(accessToken, createGroupRequest));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Метод изменения группы по id")
    public ResponseEntity<?> updateGroup(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id,
                                             @RequestBody CreateGroupRequest request) {
        groupService.updateGroupThroughRepresentation(accessToken, request, id);
        return new ResponseEntity<>(HttpStatus.OK);

    }

}
