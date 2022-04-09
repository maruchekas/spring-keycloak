package ru.maruchekas.keycloak.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.service.GroupService;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<?> getAllGroups(@RequestHeader("Authorization") String accessToken){
        return ResponseEntity.ok(groupService.getAllGroups(accessToken));

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id){
        return ResponseEntity.ok(groupService.getGroupById(accessToken));

    }

    @PostMapping
    public ResponseEntity<?> postNewGroup(@RequestHeader("Authorization") String accessToken,
                                          @RequestBody CreateGroupRequest createGroupRequest){
        return ResponseEntity.ok(groupService.createGroup(createGroupRequest, accessToken));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroupById(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable("id") String id){
        return ResponseEntity.ok(groupService.deleteGroupById(accessToken, id));

    }

}
