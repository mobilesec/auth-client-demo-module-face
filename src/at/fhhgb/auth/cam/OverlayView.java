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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author thomaskaiser
 * 
 */
public class OverlayView extends View {

	private RectF overlayRect;

	public OverlayView(Context context) {
		super(context);
	}

	public OverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RectF getOverlayRect() {
		return overlayRect;
	}

	public void setOverlayRect(RectF overlayRect) {
		this.overlayRect = overlayRect;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (overlayRect == null) return;
		
		Paint paint = new Paint();
		paint.setColor(Color.RED);

		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);
		
		RectF viewRect = convertRect(overlayRect);
		Log.v("overlayview", "onDraw: converted overlay from " + overlayRect + " to " + viewRect);
		
		canvas.drawRect(viewRect, paint);
	}

	private RectF convertRect(RectF overlayRect) {
		Matrix matrix = new Matrix();
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(findFrontCameraId(), info);
		// Need mirror for front camera.
		boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
		matrix.setScale(mirror ? -1 : 1, 1);
		// This is the value for android.hardware.Camera.setDisplayOrientation.
		matrix.postRotate(90);
		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
		matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
		
		RectF result = new RectF();
		matrix.mapRect(result, overlayRect);
		return result;
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
}
