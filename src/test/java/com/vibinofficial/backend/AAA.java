package com.vibinofficial.backend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Implement Test
public class AAA {
    // TODO: TestContainers

    @BeforeAll
    void setup() {
        /**
         * {
         *   "dev": {
         *     "auth_server": "127.0.0.1:8888",
         *     "auth_realm": "master",
         *     "auth_client": "test-client",
         *     "auth_user": "admin",
         *     "auth_pass": "admin"
         *   }
         * }
         */
        //POST http://{{ auth_server}}/auth/realms/{{ auth_realm }}/protocol/openid-connect/token
        //Content-Type: application/x-www-form-urlencoded
        //
        //client_id={{ auth_client }}&username={{ auth_user }}&password={{ auth_pass}}&grant_type=password
        //
        //> {%
        //    client.test("Request executed successfully", function() {
        //        client.assert(response.status === 200, "Response status is not 200");
        //        client.global.set('access_token', response.body.access_token)
        //    });
        //%}
        //
    }

    @Test
    void name2() {
        //###
        //GET http://localhost:8891/friendship/state/4
        //Authorization: Bearer {{ access_token }}
        //
        //> {%
        //    client.test("Request executed successfully", function() {
        //        client.assert(response.status === 200, "Response status is not 200");
        //    });
        //%}
        fail();
    }

    @Test
    void name3() {
        //GET http://localhost:8891/friendship/username
        //Authorization: Bearer {{ access_token }}
        //
        //> {%
        //    client.test("Request executed successfully", function() {
        //        client.assert(response.status === 200, "Response status is not 200");
        //    });
        //%}
        fail();
    }
}
