package com.appsdeveloperblog.app.ws.ui.controller;

import com.appsdeveloperblog.ws.ui.model.response.AddressesRest;
import com.appsdeveloperblog.ws.ui.model.response.ErrorMessages;
import com.appsdeveloperblog.ws.ui.model.response.OperationStatusModel;
import com.appsdeveloperblog.ws.ui.model.response.UserRest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.service.AddressService;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;
//import com.appsdeveloperblog.app.ws.shared.Utils;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.ws.ui.model.request.PasswordResetModel;
import com.appsdeveloperblog.ws.ui.model.request.PasswordResetRequestModel;
import com.appsdeveloperblog.ws.ui.model.request.UserDetailsRequestModel;

/*
Marks this class as a RestController.
Helps in receiving HTTP requests
 */

/*
Each HTTP request contains a path.
This helps allocate a path to the below methods. ('users') in this case
 */
@RestController
@RequestMapping("users") // http://localhost:8080/users
@CrossOrigin(origins="*")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressesService;

	@Autowired
	AddressService addressService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();

		UserDto userDto = userService.getUserbyUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);

		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="Bearer JWT Token",paramType="header")
	})
	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest postUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

		UserRest returnValue = new UserRest();

		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELDS.getErrorMessage());

//        UserDto userDto = new UserDto();
		// Commenting because BeanUtils isn't the best at copying Java Objects. Only
		// good for properties copying.
//        BeanUtils.copyProperties(userDetails,userDto);

		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);

		UserDto createdUser = userService.createUser(userDto);
//        BeanUtils.copyProperties(createdUser,returnValue);
		returnValue = modelMapper.map(createdUser, UserRest.class);

		return returnValue;
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="Bearer JWT Token",paramType="header")
	})
	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();

		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELDS.getErrorMessage());

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updatedUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="Bearer JWT Token",paramType="header")
	})
	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {

		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName("DELETE");

		userService.deleteUser(id);
		returnValue.setOperationResult("SUCCESS");
		return returnValue;
	}
	
	
	@ApiImplicitParams({
		@ApiImplicitParam(name="authorization", value="Bearer JWT Token",paramType="header")
	})
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {

		List<UserRest> returnValue = new ArrayList<>();
		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}

		return returnValue;
	}

	@GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> returnValue = new ArrayList<>();

		List<AddressDto> addressesDto = addressesService.getAddresses(id);

		if (addressesDto != null && !addressesDto.isEmpty()) {
			java.lang.reflect.Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			returnValue = new ModelMapper().map(addressesDto, listType);

			for (AddressesRest addressRest : returnValue) {
				Link selfLink = WebMvcLinkBuilder.linkTo(
						WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(id, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(selfLink);
			}
		}

		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(id))
				.withSelfRel();

		return CollectionModel.of(returnValue, userLink, selfLink);
	}

	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
//    public AddressesRest getUserAddress(@PathVariable String userId, @PathVariable String addressId)
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDto addressesDto = addressService.getAddress(addressId);

		ModelMapper modelMapper = new ModelMapper();
		AddressesRest returnValue = modelMapper.map(addressesDto, AddressesRest.class);
		// http://localhost:8080/users/<userId>
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link userAddressesLink = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(userId))
//    			.slash(userId)
//    			.slash("addresses")
				.withRel("addresses");
		Link selfLink = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(userId, addressId))
//    			.slash(userId)
//    			.slash("addresses")
//    			.slash(addressId)
				.withSelfRel();

//    	returnValue.add(userLink);
//    	returnValue.add(userAddressesLink);
//    	returnValue.add(selfLink);

		return EntityModel.of(returnValue, Arrays.asList(userLink, userAddressesLink, selfLink));

	}

	@GetMapping(path = "/email-verification", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName("VERIFY_EMAIL");

		boolean isVerified = userService.verifyEmailToken(token);

		if (isVerified) {
			returnValue.setOperationResult("SUCCESS");
		} else {
			returnValue.setOperationResult("FAILURE");
		}

		return returnValue;
	}
	
//	@PostMapping(path = "/password-reset-request",
//			consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
//			produces = { MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE })
//	public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel)
//	{
//		OperationStatusModel returnValue = new OperationStatusModel();
//		
//		boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
//		
//		returnValue.setOperationName("REQUEST_PASSWORD_RESET");
//		returnValue.setOperationResult("ERROR");
//		
//		if(operationResult)
//		{
//			returnValue.setOperationResult("SUCCESS");
//		}
//		
//		
//		return returnValue;
//	}
	
	@PostMapping(path = "/password-reset",
			consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel)
	{
		OperationStatusModel returnValue = new OperationStatusModel();
		
		boolean operationResult = userService.resetPassword(
				passwordResetModel.getToken(),
				passwordResetModel.getPassword());
		
		returnValue.setOperationName("PASSWORD_RESET");
		returnValue.setOperationResult("ERROR");
		
		if(operationResult)
		{
			returnValue.setOperationResult("SUCCESS");
		}
	
		
		return returnValue;
	}

}
