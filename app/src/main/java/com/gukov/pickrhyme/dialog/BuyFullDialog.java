package com.gukov.pickrhyme.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.databinding.DialogBuyFullBinding;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class BuyFullDialog extends BottomSheetDialogFragment implements BillingProcessor.IBillingHandler {

    private DialogBuyFullBinding binding;
    private Context context;
    private BillingProcessor bp;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBuyFullBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        bp = BillingProcessor.newBillingProcessor(context, getString(R.string.billing_google_license_key), this);
        bp.initialize();

        binding.btnBuyFullApp.setOnClickListener(v -> {
            bp.purchase(getActivity(), getString(R.string.billing_google_product_id)); // покупаем
        });

        return binding.getRoot();
    }

    // обновление интерфейса после удачной покупки
    private void refreshFragment() {
        binding.tvTitle.setVisibility(View.GONE);
        binding.btnBuyFullApp.setVisibility(View.GONE);
        binding.tvAdvantages.setText(R.string.restart_app);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, PurchaseInfo details) {
        refreshFragment();
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (error != null)
            Toasty.error(context, Objects.requireNonNull(error.getMessage(), "error.getMessage() must not be null"), Toast.LENGTH_LONG, false).show();
    }

    @Override
    public void onBillingInitialized() {
    }

}
