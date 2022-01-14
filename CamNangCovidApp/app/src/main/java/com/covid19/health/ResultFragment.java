package com.covid19.health;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.support.v7.app.AlertDialog;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

//import com.dd.processbutton.iml.ActionProcessButton;

import androidx.fragment.app.DialogFragment;

import java.io.File;

import com.covid19.health.helper.FileUtils;
import com.covid19.health.request.ApiConfig;
import com.covid19.health.request.AppConfig;
import com.covid19.health.request.ServerResponse;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class ResultFragment extends DialogFragment {
    private ImageButton testImage;
    private ImageView cancelBtn;
    private CircularProgressButton btnUpload;
    private Uri selectedImageUri;
    int SELECT_PICTURE = 200;

    public static ResultFragment newInstance(String result) {
        ResultFragment f = new ResultFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("text_result", result);
        f.setArguments(args);
        return f;
    }

    private void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    testImage.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void uploadFile(String predictCovid, final AlertDialog alert) throws Exception {
        String sourceFileUri = getContext().getCacheDir().getAbsolutePath() + "/record.wav";

        File audioFile = new File(sourceFileUri);
        RequestBody audio_body = RequestBody.create(MediaType.parse("audio/*"), audioFile);
        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

//        File imageFile = FileUtils.getFileFromUri(getContext(), selectedImageUri);
        File imageFile = new File(selectedImageUri.getPath());
        RequestBody image_body = RequestBody.create(MediaType.parse("image/*"), imageFile);
        RequestBody predict_result = RequestBody.create(MediaType.parse("text/plain"), predictCovid);
        Call<ServerResponse> call = getResponse.uploadFile(audio_body, image_body, predict_result);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        btnUpload.revertAnimation();
//                        btnUpload.setProgress(100);
//                        Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        btnUpload.revertAnimation();
//                        btnUpload.setProgress(-1);
//                        Toast.makeText(getActivity().getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_result, null);
        builder.setView(v);
        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);

        cancelBtn = (ImageView) v.findViewById(R.id.cancelDialog);
        cancelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.cancel();
                    }
                });

        testImage = (ImageButton) v.findViewById(R.id.testResult);
        testImage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageChooser();
                    }
                });

//        final ProgressGenerator progressGenerator = new ProgressGenerator();
        final String resultString = getArguments().getString("text_result");
        btnUpload = (CircularProgressButton) v.findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                progressGenerator.start(btnUpload);
//                btnUpload.setEnabled(false);
                btnUpload.startAnimation();
                try {
                    uploadFile(resultString, alert);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return alert;
    }

}
