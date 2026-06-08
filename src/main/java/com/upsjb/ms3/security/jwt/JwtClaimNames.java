package com.upsjb.ms3.security.jwt;

public final class JwtClaimNames {

    public static final String ID_USUARIO_MS1 = "id_usuario_ms1";
    public static final String ID_USUARIO_MS1_CAMEL = "idUsuarioMs1";
    public static final String ID_USUARIO = "id_usuario";
    public static final String ID_USUARIO_CAMEL = "idUsuario";

    public static final String USER_ID = ID_USUARIO_MS1;
    public static final String USER_ID_ALT = "user_id";
    public static final String USER_ID_CAMEL = "userId";

    public static final String EMPLOYEE_ID = "id_empleado_ms2";
    public static final String EMPLOYEE_ID_CAMEL = "idEmpleadoMs2";

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
    public static final String SESSION_ID_CAMEL = "sessionId";
    public static final String ID_SESION = "id_sesion";
    public static final String ID_SESION_CAMEL = "idSesion";

    public static final String TOKEN_TYPE = "typ";
    public static final String TOKEN_TYPE_ALT = "token_type";
    public static final String TOKEN_TYPE_CAMEL = "tokenType";
    public static final String TIPO_TOKEN = "tipo_token";
    public static final String TIPO_TOKEN_CAMEL = "tipoToken";

    public static final String SUBJECT = "sub";
    public static final String AUDIENCE = "aud";
    public static final String ISSUER = "iss";

    private JwtClaimNames() {
    }
}