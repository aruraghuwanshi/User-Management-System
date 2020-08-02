package com.appsdeveloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.entity.PasswordResetTokenEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.PasswordResetTokenRepository;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.AmazonSES;
import com.appsdeveloperblog.app.ws.shared.Utils;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.ws.ui.model.response.ErrorMessages;
import com.appsdeveloperblog.ws.ui.model.response.UserRest;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Override
	public UserDto createUser(UserDto user)
	{
		if(userRepository.findByEmail(user.getEmail()) != null) 
			throw new RuntimeException("Record already exists");
		
		for(int i=0;i<user.getAddresses().size();i++)
		{
			AddressDto address = user.getAddresses().get(i);
			address.setUserDetails(user);
			address.setAddressId(utils.generateAddressId(30));
			user.getAddresses().set(i, address);
		}
		
		ModelMapper modelMapper = new ModelMapper();
//		UserEntity userEntity= new UserEntity();
//		BeanUtils.copyProperties(user, userEntity);
		
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);
		
		
		
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(true);
		
		
		UserEntity storedUserDetails = userRepository.save(userEntity);
		
		UserDto userDto = new UserDto();
//		BeanUtils.copyProperties(storedUserDetails, userDto);
		userDto = modelMapper.map(storedUserDetails, UserDto.class);
		
		//Send user an email for email verification
		new AmazonSES().verifyEmail(userDto);
		
		return userDto;
	}
	
	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if(userEntity == null) throw new UsernameNotFoundException(email);
		
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userEntity, userDto);
		return userDto;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {   //Standard called method
		UserEntity userEntity = userRepository.findByEmail(email);
		if(userEntity == null) throw new UsernameNotFoundException(email);
		
//		return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),userEntity.getEmailVerificationStatus(),
//				true, true, true, new ArrayList<>());
		
		return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),new ArrayList<>());
	}

	@Override
	public UserDto getUserbyUserId(String userId) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if(userEntity == null) throw new UsernameNotFoundException("User with userID:"+userId+" not Found");
		
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto updateUser(String id, UserDto user) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(id);
		if(userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());
		
		UserEntity updatedUserDetails = userRepository.save(userEntity);
		
		BeanUtils.copyProperties(updatedUserDetails, returnValue);
		
		return returnValue;
	}

	@Override
	public void deleteUser(String id) {
		UserEntity userEntity = userRepository.findByUserId(id);
		if(userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List <UserDto> returnValue = new ArrayList<>();
		Pageable pageableRequest = PageRequest.of(page, limit);
		
		Page <UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();
		
		for(UserEntity userEntity: users)
	    {
	    	UserDto userDto = new UserDto();
	    	BeanUtils.copyProperties(userEntity, userDto);
	    	returnValue.add(userDto);
	    }
		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		
		boolean returnValue = false;
		
		//Find user by token
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		
		if(userEntity != null)
		{
			boolean hasTokenExpired = Utils.hasTokenExpired(token);
			if(!hasTokenExpired)
			{
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}
		return returnValue;
	}

//	@Override
//	public boolean requestPasswordReset(String email) {
//		boolean returnValue = false;
//		
//		UserEntity userEntity = userRepository.findByEmail(email);
//		
//		if(userEntity == null)
//		{
//			return returnValue;
//		}
//		
//		String token = Utils.generatePasswordResetToken(userEntity.getUserId());
//		
//		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
//		passwordResetTokenEntity.setToken(token);
//		passwordResetTokenEntity.setUserDetails(userEntity);
//		passwordResetTokenRepository.save(passwordResetTokenEntity);
//		
//		returnValue = new AmazonSES().sendPasswordResetRequest(
//				userEntity.getFirstName(),
//				userEntity.getEmail(),
//				token);
//	
//		return returnValue;
//	}

	@Override
	public boolean resetPassword(String token, String password) {
		boolean returnValue = false;
		
		if(Utils.hasTokenExpired(token))
		{
			return returnValue;
		}
		
		PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
		
		if(passwordResetTokenEntity == null)
		{
			return returnValue;
		}
		
		//prepare new password
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		
		//Update user Password
		UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
		userEntity.setEncryptedPassword(encodedPassword);
		UserEntity savedUserEntity = userRepository.save(userEntity);
		
		//Verify if password was saved successfully
		if(savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword))
		{
			returnValue = true;
		}
		
		//Remove Password Reset Token from the database
		passwordResetTokenRepository.delete(passwordResetTokenEntity);
		return returnValue;
	}



	
}





