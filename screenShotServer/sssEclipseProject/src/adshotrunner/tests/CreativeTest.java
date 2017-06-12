package adshotrunner.tests;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import adshotrunner.Creative;

public class CreativeTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Getting Creative");
		Creative tempCreative = Creative.get(1);
		System.out.println("Got creative");
		System.out.println(tempCreative.id());
		System.out.println(tempCreative.uuid());
		System.out.println(tempCreative.imageFilename());
		System.out.println(tempCreative.imageURL());
		System.out.println(tempCreative.image());
		System.out.println(tempCreative.width());
		System.out.println(tempCreative.height());
		System.out.println(tempCreative.priority());
		System.out.println(tempCreative.tagScript());
		System.out.println(tempCreative.tagPageURL());
		System.out.println(tempCreative.status());
		System.out.println(tempCreative.errorMessage());
		System.out.println(tempCreative.createdTimestamp());
		System.out.println(tempCreative.queuedTimestamp());
		System.out.println(tempCreative.processingTimestamp());
		System.out.println(tempCreative.finishedTimestamp());
		System.out.println(tempCreative.errorTimestamp());
		System.out.println();
		
//		System.out.println("Original");
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println();
//
//		System.out.println("QUEUED");
//		tempCreative.setStatus(Creative.QUEUED);
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//
//		System.out.println("PROCESSING");
//		tempCreative.setStatus(Creative.PROCESSING);
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println();
//
//		System.out.println("FINISHED");
//		tempCreative.setStatus(Creative.FINISHED);
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println();
//
//		System.out.println("ERROR");
//		tempCreative.setError("new error message");
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println(tempCreative.errorMessage());
//		System.out.println();
		
//		System.out.println("Setting image url");
//		tempCreative.setImageURL("https://s3.amazonaws.com/asr-development/creativeimages/8edc627f-32d9-4dd0-843b-6491c748ee50.png");
//		System.out.println(tempCreative.imageURL());
//		System.out.println(tempCreative.image());
//		System.out.println(tempCreative.width());
//		System.out.println(tempCreative.height());

		System.out.println("Setting image");
		BufferedImage creativeImage = null; 
		try {
			URL imageURL = new URL("https://s3.amazonaws.com/asr-development/creativeimages/0014d0d8-e44f-472b-a2c2-29572763ffac.png?response-content-disposition=inline&X-Amz-Security-Token=AgoGb3JpZ2luEPP%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCXVzLWVhc3QtMSKAAgeKmoysH8N1qT8gmRpptxBoXzjrymdxC7zvfN8hX0bl7NYoti%2FOQZlHS4%2BeYdukJTlc45FneTHegnWv4%2BG1rO76TouBMsdCZKH5egm06zBc5PKl9SBQpL0GXSUIfZdLiyu9eldvMi7RKcBUol5sJ6TNwzilS14cLSeJxxYXeEJDXxFgs%2F6aAIVhiM6KZodKKTVbeDpvOrXO7ruHq3PDchs6Y6D9lwsmOQhEd%2BVZqw%2FuBmEMUVXR7wlXd18v75IlzSWagtjtjbbSXKk7afC8D24wuyPwGZaWpZ4kZNw%2FDf%2BOD0Oxf1tXj2m5AFGOC9rusZYjAosWvI%2FGeLGKNWUVyiIq8gMIOBABGgw0Njk2NTg0MDQxMDgiDEfMpaffVXtlErDbMSrPA%2B%2Fs0LFS2Rjg%2Bzp1gGua1GIL%2FW0OtLI54gyzmtV8%2BksCBy8o6n50IQojUgsVv%2Bwm3Km5uG6hZ9sogWVLcqxTDqpT%2F6NK7nMCd3ASppyZUK%2BnuZg6nRdiXb%2Ff5i3%2FR4fcm6ilG%2B4WmuGusX%2FLZYvp02JkEVxd3dalKLtn3%2FOGkNaw9XQIn4zIz2VLWxP%2FdD40xxSWA7w8aovr80M%2FG7xqXyMtHa9ZwOdnueJin9OVm6I5z%2FaMC6LaKfxGuP%2BSg5B%2FYqxs0b8Rnz8%2BegS4D1WvvYttgW%2BzGURHycgBPj2q9gfbVMrjnFfhQtc6aWiD12XqhF%2BtC3ltZNbNyt73iKsEZjuVfU6qPWY%2Fhmsw53l2pMfOKgcV5dIWQ08HrFI8n3TgsQA4Pq%2B6qcRrnmlC8giU85mw4Fo50gDR%2B33ybZ842PSFuUdn5Vf1D%2BjW%2BTXCLWkXNd5Z6GmSdhSiMrgox2KpEenZnV1gC%2FeLkLEcM1jUj3mHiwSD%2FzDRHbHQYTJm5I9DH6HPai2O4aOLECAeOIXo5k1fQoovoTxlsOP0cmVqGUP6qWAeMzn2DJr2aV7IPuelSBI%2Bp3jyr55UJNkqwb4B3ifEPWOK1pNHJa%2FElMyzXowwzeTcyQU%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20170606T225318Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=ASIAJ7RY7AGPWWBDTTSQ%2F20170606%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=56dcd0d32709844cb35e4984beca2ad7022b35eff4c4e8f86172ae3df22a70af");
			creativeImage = ImageIO.read(imageURL); 
		}
        catch (IOException e) {
        	e.printStackTrace();;
        }
		
		
		
		tempCreative.setImage(creativeImage);
		System.out.println(tempCreative.imageFilename());
		System.out.println(tempCreative.imageURL());
		System.out.println(tempCreative.image());
		System.out.println(tempCreative.width());
		System.out.println(tempCreative.height());

	}

}
