package com.appsdeveloperblog.app.ws.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Jwts;

public class AuthorizationFilter extends BasicAuthenticationFilter{

	public AuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req,
									HttpServletResponse res,
									FilterChain chain)throws IOException, ServletException{
		String header = req.getHeader(SecurityConstants.HEADER_PREFIX);
		if(header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX))
		{
			chain.doFilter(req,res);
			return;
		}
		
		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req,res);
	}
	
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
	{
		String token = request.getHeader(SecurityConstants.HEADER_PREFIX);
		
		if(token != null)
		{
			token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
			
			String user = Jwts.parser()
						  .setSigningKey( SecurityConstants.getTokenSecret())
						  .parseClaimsJws( token )
						  .getBody()
						  .getSubject();
			
			if(user != null)
			{
				return new UsernamePasswordAuthenticationToken(user, null, new ArrayList());
			}
			return null;
		}
		return null;
	}
}