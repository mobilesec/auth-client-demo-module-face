/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.fhhgb.auth.cam;

import android.app.Activity;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class PreviewDemo extends Activity {
	protected static final String TAG = "PreviewDemo";
	private OverlayView overlay;
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private int frontCameraId;
	private Face[] detectedFaces;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preview = (SurfaceView) findViewById(R.id.preview);
		overlay = (OverlayView) findViewById(R.id.overlay);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		frontCameraId = findFrontCameraId();
	}
	

	@Override
	public void onResume() {
		super.onResume();
		camera = Camera.open(frontCameraId);
		camera.setDisplayOrientation(90);
		camera.setFaceDetectionListener(new MyFaceDetectionListener());
	}

	private int findFrontCameraId() {
		int numberOfCameras = Camera.getNumberOfCameras();

		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				return i;
			}
		}
		return 0;
	}


	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopFaceDetection();
			camera.stopPreview();
		}

		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}
	
	public void startFaceDetection(){
	    Camera.Parameters params = camera.getParameters();

	    // start face detection only *after* preview has started
	    if (params.getMaxNumDetectedFaces() > 0){
	        camera.startFaceDetection();
	    }
	}
	
	class MyFaceDetectionListener implements Camera.FaceDetectionListener {

	    @Override
	    public void onFaceDetection(Face[] faces, Camera camera) {
	    	detectedFaces = faces;
	        if (faces.length > 0){
//	            Log.d(TAG, "face detected at: "+ faces[0].rect );
	            RectF overlayRect = new RectF(faces[0].rect);
	            overlay.setOverlayRect(overlayRect);
	            overlay.postInvalidate();
	        }
	    }
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(PreviewDemo.this, t.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = getBestPreviewSize(width, height, parameters);
			Log.d(TAG, "Found best preview size: " + size.width + "/" + size.height + " for width/height:" 
					+ width + "/" + height);

			if (size != null) {
				parameters.setPreviewSize(size.width, size.height);
				camera.setParameters(parameters);
				camera.startPreview();

				startFaceDetection();
				inPreview = true;
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};
}