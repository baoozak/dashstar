package io.github.baoozak.dashstar.model;

import lombok.Data;

@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色
     */
    private String role;
}
