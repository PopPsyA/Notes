package com.ru.devit.notes.presentation.noteadddialog;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ru.devit.notes.R;
import com.ru.devit.notes.data.ImageSaverToDisk;
import com.ru.devit.notes.presentation.utils.RandomColor;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NoteAddDialog extends DialogFragment {

    private EditText mEditTextTitle , mEditTextDesc;
    private ImageView mImageViewFromGallery;
    private Button mBtnPickUpFromGallery;
    private NoteAddDialogListener listener;
    private ImageSaverToDisk imageSaverToDisk;
    private Uri resultImagePath;
    private static final int REQUEST_CODE_MULTIPLY_IMAGE = 24;
    private static final int PERMISSION_READ_WRITE_REQUEST_CODE = 56;

    public interface NoteAddDialogListener {
        void onAddBtnFromDialogClicked(String noteTitle , String noteDesc , String imgPath , int noteColor);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageSaverToDisk = new ImageSaverToDisk();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NoteAddDialogListener) context;
        } catch (ClassCastException exception){
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        requestPermission();

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_note , null);
        mEditTextTitle = view.findViewById(R.id.et_note_title);
        mEditTextDesc = view.findViewById(R.id.et_note_desc);
        mImageViewFromGallery = view.findViewById(R.id.iv_image_from_gallery);
        mBtnPickUpFromGallery = view.findViewById(R.id.btn_pick_up_image_from_gallery);

        mBtnPickUpFromGallery.setOnClickListener(v -> {
            selectPictureFromGallery();
            mBtnPickUpFromGallery.setVisibility(View.GONE);
            mImageViewFromGallery.setVisibility(View.VISIBLE);
        });
        builder.setView(view)
                .setTitle(getString(R.string.note_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.add), (dialogInterface, i) -> {
                    String noteTitle = mEditTextTitle.getText().toString();
                    String noteDesc = mEditTextDesc.getText().toString();
                    try {
                        if (resultImagePath == null){
                            listener.onAddBtnFromDialogClicked("", "" , null , 0);
                            return;
                        }
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver() ,
                                resultImagePath);
                        imageSaverToDisk.saveImage(bitmap , noteTitle)
                                .subscribe(() -> listener.onAddBtnFromDialogClicked(noteTitle, noteDesc , resultImagePath.toString() , RandomColor.generetaRandomColor()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dismiss());
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_MULTIPLY_IMAGE){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    resultImagePath = selectedImageUri;
                    renderImage(resultImagePath);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_READ_WRITE_REQUEST_CODE : {

            }
        }
    }

    private void renderImage(Uri imgPath){
        Picasso.get()
                .load(imgPath)
                .fit()
                .centerCrop()
                .into(mImageViewFromGallery);
    }

    private void selectPictureFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent , getString(R.string.select_image)) , REQUEST_CODE_MULTIPLY_IMAGE);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity() ,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE ,
                        Manifest.permission.READ_EXTERNAL_STORAGE} ,
                PERMISSION_READ_WRITE_REQUEST_CODE);
    }
}
