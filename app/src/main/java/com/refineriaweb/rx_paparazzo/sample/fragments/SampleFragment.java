package com.refineriaweb.rx_paparazzo.sample.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.refineriaweb.rx_paparazzo.library.RxPaparazzo;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.sample.R;
import com.refineriaweb.rx_paparazzo.sample.adapters.ImagesAdapter;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;

public class SampleFragment extends Fragment {
    private ImageView imageView;
    private RecyclerView recyclerView;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sample_layout, container, false);
        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        imageView = (ImageView) getView().findViewById(R.id.iv_image);
        recyclerView = (RecyclerView) getView().findViewById(R.id.rv_images);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        getView().findViewById(R.id.fab_camera).setOnClickListener(v -> captureImage());
        getView().findViewById(R.id.fab_camera_crop).setOnClickListener(v -> captureImageWithCrop());
        getView().findViewById(R.id.fab_pickup_image).setOnClickListener(v -> pickupImage());
        getView().findViewById(R.id.fab_pickup_images).setOnClickListener(v -> pickupImages());
    }

    private void captureImage() {
        RxPaparazzo.takeImage(this)
                .size(Size.Original)
                .usingCamera()
                .subscribe(response -> {

                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void captureImageWithCrop() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        options.setMaxBitmapSize(1000000000);

        RxPaparazzo.takeImage(this)
                .crop(options)
                .usingCamera()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        options.setMaxBitmapSize(1000000000);

        RxPaparazzo.takeImage(this)
                .crop(options)
                .size(Size.Small)
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void pickupImages() {
        RxPaparazzo.takeImages(this)
                .crop()
                .size(Size.Screen)
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    if (response.data().size() == 1) response.targetUI().loadImage(response.data().get(0));
                    else response.targetUI().loadImages(response.data());
                });
    }

    private void loadImage(String filePath) {
        imageView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        File imageFile = new File(filePath);

        Picasso.with(getActivity()).setLoggingEnabled(true);
        Picasso.with(getActivity()).invalidate(new File(filePath));
        Picasso.with(getActivity()).load(imageFile).into(imageView);
    }

    private void showUserCanceled() {
        Toast.makeText(getActivity(), getString(R.string.user_canceled), Toast.LENGTH_SHORT).show();
    }

    private void loadImages(List<String> filesPaths) {
        imageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(new ImagesAdapter(filesPaths));
    }
}