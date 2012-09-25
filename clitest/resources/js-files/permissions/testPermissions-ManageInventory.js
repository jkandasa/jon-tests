
// currently failing, see bug https://bugzilla.redhat.com/show_bug.cgi?id=841625 - Runs only under JON 

/**
* creates test Role with Manage Inventory permission, creates user having that Role, checks manage Inventory permissions, removes manage inventory from roles, re-checks permissions.
* TCMS TestCases - 174639 , 174725
*/


/**
 * @author ahovsepy@redhat.com (Armine Hovsepyan)
 */
var userName = "testUserRemote";
var firstName = "firstName";
var lastName = "lastName";
var email = "ahovesepy@redhat.com";
var password = "password";

var roleName = "testRoleRemoteManageInv";
var roleDescription = "test Role Description Remote";

var roleIds = new Array();
var userIds = new Array();

var permissionManageInventory = Permission.MANAGE_INVENTORY;

var permissions  =  new Array();
permissions.push(permissionManageInventory);


//call create Role with View users permission
var savedRole = createRoleWithPermission(roleName,roleDescription, permissions);

roleIds.push(savedRole.getId());


//call create user function
var createdUser = createUser(userName, firstName, lastName, email, password);

userIds.push(createdUser.getId());

//add Role
addRoleToUser(userIds[0], roleIds);

//verify manage inventory permission granted
//log in with creted user 
var logedInUser = SubjectManager.login(userName, password);
//verify manage inventory permission
verifyManageInventoryPermission(logedInUser, true);


//verify manage inventory permission not granted
//update Role 
removePermissionFromRole(savedRole, permissions );
//add Role
addRoleToUser(userIds[0], roleIds);
//log in with creted user 
var logedInUser = SubjectManager.login(userName, password);
//verify manage inventory permission
verifyManageInventoryPermission(logedInUser, false);



//call delete role function
deleteRole(roleIds);
//call delete user
deleteUser(userIds);


/** 
 *  Function - create Role 
 *  @param - permissions list 
 *  @return - created/saved Role
 */
function createRoleWithPermission(roleName, roleDescription, permissions) {
	var role = new Role();
	role.name = roleName;
	role.description = roleDescription;
	var i = 0;
	for ( i = 0; i < permissions.length; i++){
	     role.addPermission(permissions[i]);
	}
	var roleNew = RoleManager.createRole(role);
	println("roleNew ..." + roleNew);

return roleNew;
}


 
/** 
 * Function create user
 * @param - username, firstName, lastname, password, email
 * @return - created user
 */

function createUser(userName, firstName, lastName, email, password) {
	var s = new Subject(userName, true, false);
	s.setFirstName(firstName);
	s.setLastName(lastName);
        s.setEmailAddress(email);
	
	var newUser = SubjectManager.createSubject(s);
	SubjectManager.createPrincipal(newUser.getName(), "password");

	return newUser;
}


/** 
 * Function add role to user
 * @param - userSubject, roleId[]
 * @return 
 */

function addRoleToUser(userId, roleId){
	RoleManager.addRolesToSubject(userId, roleId );
}

/** 
 * Function add user to role
 * @param - userId, userSubject[]
 * @return 
 */

function addUserToRole(roleId, userId){
	RoleManager.addSubjectsToRole(userId, roleId );
}


/** 
 *  Function - delete Role 
 *  @param - role ids list 
 *  @return 
 */

function deleteRole(roleIds) {
 	RoleManager.deleteRoles(roleIds);
}


 /** 
 *  Function - remove permission from Role 
 *  @param - role , permissions[] list 
 *  @return 
 */

function removePermissionFromRole(role, permissions) {

for ( i = 0; i < permissions.length; i++){
	     role.removePermission(permissions[i]);
	}
RoleManager.updateRole(role);
}



/** 
 *  Function - delete Role 
 *  @param - user ids list 
 *  @return 
 */

function deleteUser(userIds) {
 	SubjectManager.deleteSubjects(userIds);
}


/** 
 *  Function - verify manage inventory functionality
 *  @param -  logedInUser, bool - manage inventory permission Activated/Inactivated (boolean)
 *  @return - 
 */
function verifyManageInventoryPermission(logedInUser, bool) {


try {

var rc = new ResourceCriteria();
var resources = ResourceManager.findResourcesByCriteria(logedInUser,rc);

	if(bool){
		assertTrue(resources.size() > 1, "Manage Inventory permission doesnt work correctly!!");
	  } else {
		 assertFalse(resources.size() > 0, "Manage Inventory permission doesnt work correctly!!");
                 }
    } catch(err) {
 	if (err.message.toString().indexOf("Manage Inventory permission doesnt work correctly!!") !=-1) {
	    	println("Manage Inventory permission doesnt work correctly!!");
       
	   } 
        //call delete role function
	deleteRole(roleIds);

	//call delete user
	deleteUser(userIds);
      
      } 
	 
}

 
