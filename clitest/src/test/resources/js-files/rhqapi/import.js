// this test should run when at least 1 platform is waiting in discovery queue


importPlatformWithChildren();
//importPlatformWithoutChildren();
//importResource();
println("Waiting 5min after platform import")
sleep(1000*60*5);

function importPlatformWithChildren() {
	sleep(15*1000); // wait 'till platform appears in disco-queue
	// using importPlatform, we import it including children resources (default)
	var imported = discoveryQueue.importPlatform(agent);
	assertTrue(imported.exists(),"Imported platform exists in inventory");
	// let's wait until our platform becomes available
	imported.waitForAvailable();
	assertTrue(imported.isAvailable(),"Imported platform is available");
	assertTrue(imported.children().length>0,"At least 1 child resource was imported on platform");
	// let's uninventory this platform
	//imported.uninventory();
	//assertFalse(imported.exists(),"Imported platform exists in inventory");
}


function importPlatformWithoutChildren() {
	sleep(15*1000); // wait 'till platform appears in disco-queue
	var platforms = Inventory.discoveryQueue.listPlatforms();
	assertTrue(platforms.length>0,"There is at least one platform in discovery queue");
	// using importPlatform, we import it without children resources
	var imported = Inventory.discoveryQueue.importPlatform(platforms[0].getProxy().getName(),false);
    assertTrue(imported.exists(),"Imported platform exists in inventory");
	// let's wait until our platform becomes available
	imported.waitForAvailable();
	assertTrue(imported.isAvailable(),"Imported platform is available");
	// we check for RHQ Agent - every platform has this as sub-resource
    assertTrue(imported.children({name:"RHQ Agent",resourceTypeName:"RHQ Agent"}).length==0,"Platform was imported without children - RHQ Agent is present in inventory");
	// let's uninventory this platform
	imported.uninventory();
	assertFalse(imported.exists(),"Imported platform exists in inventory");
}

function importResource() {
	sleep(15*1000); // wait 'till platform appears in disco-queue
	var platforms = Inventory.discoveryQueue.listPlatforms();
	assertTrue(platforms.length>0,"There is at least one platform in discovery queue");
	// using importResource, we import it without children resources
	var imported = Inventory.discoveryQueue.importResource(platforms[0]);
	assertTrue(imported.exists(),"Imported platform exists in inventory");
	// let's wait until our platform becomes available
	imported.waitForAvailable();
	assertTrue(imported.isAvailable(),"Imported platform is available");
	// we HAVE to wait here until https://bugzilla.redhat.com/show_bug.cgi?id=830158 is properly fixed
	sleep(20*1000);
	// let's uninventory this platform
	imported.uninventory();
	assertFalse(imported.exists(),"Imported platform exists in inventory");
}
