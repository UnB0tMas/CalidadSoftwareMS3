package com.upsjb.ms3.security.jwt;

public final class JwtClaimNames {

    public static final String USER_ID = "id_usuario_ms1";
    public static final String USER_ID_ALT = "user_id";

    public static final String EMPLOYEE_ID = "id_empleado_ms2";

    public static final String USERNAME = "username";
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String NAME = "name";

    public static final String EMAIL = "email";

    public static final String ROL = "rol";
    public static final String ROLE = "role";
    public static final String ROLES = "roles";
    public static final String AUTHORITIES = "authorities";

    public static final String SESSION_ID = "sid";
    public static final String SESSION_ID_ALT = "session_id";

    public static final String TOKEN_TYPE = "typ";
    public static final String TOKEN_TYPE_ALT = "token_type";

    public static final String SUBJECT = "sub";
    public static final String AUDIENCE = "aud";
    public static final String ISSUER = "iss";

    private JwtClaimNames() {
    }
}