package com.jlbabon.practice.springsecurity.config;

import com.jlbabon.practice.springsecurity.config.jwt_service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

//1. You should implements OncePerRequest
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, // Our Request
            HttpServletResponse response, // Our Response
            FilterChain filterChain // Contains all filter tool we need
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        //CHECK IF JWT TOKEN EXIST IN HEADER
        if(Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response); // pass req and res to the next filter
            return;
        }

        jwt = authHeader.substring(7); // get the token Bearer_ = 6 so token starts with 7
        userEmail = jwtService.extractUsername(jwt);// todo extract the userEmail from JWT Token
        if(!Objects.isNull(userEmail) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())){
            //this code check is the user is authenticated
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            // Check if the token is valid or not
            if(jwtService.isTokenValid(jwt, userDetails)){

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            }

            filterChain.doFilter(request, response);

        }
    }
}
