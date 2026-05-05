package com.demo.EmployeeDb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

// Utility class for JSON Web Token (JWT)
public class JwtUtil {

	// HMAC-SHA256 key length requirements
    private static final String SECRET = "mysecretkey123456789mysecretkey123"; 
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // (header.payload.signature)
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // Embeds username as the "sub" 
                .setIssuedAt(new Date()) // Token creation time as "iat"
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // Expires in 1 hr as "exp" claim
                .signWith(key) // signs with HMAC-SHA256 using the static key
                .compact(); // Builds the final encoded string
    }

    // Validating JWT token
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()        
                    .setSigningKey(key) // Provides the key to verify the token
                    .build()
                    .parseClaimsJws(token); // Parses and validates full token
            return true; // Reached only if parsing succeeded
        } catch (Exception e) { // if token is expired or invalid
            return false;
        }
    }

    public static String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // Verifies signature 
                .build()
                .parseClaimsJws(token)
                .getBody() // Retrieves the claim (sub, iat, exp etc)
                .getSubject(); // Extracts the "sub" claim - emloyee's username
    }
}