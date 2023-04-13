package com.example.app_cotizacion;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.example.app_cotizacion.adapter.MaterialAdapter;
import com.example.app_cotizacion.model.Material;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class cards extends Fragment {
    private RecyclerView mRecycler;
    private FirebaseFirestore db;
    private MaterialAdapter mAdapter;
    private Button calculate, reload;
    private View view;

    private String sprice;

    private double price, total;

    private int amount;

    public cards() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cards, container, false);
        mRecycler = view.findViewById(R.id.containerRV);
        calculate = view.findViewById(R.id.calculate);
        reload = view.findViewById(R.id.reload);

        db = FirebaseFirestore.getInstance();
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        Query query = db.collection("materials");

        FirestoreRecyclerOptions<Material> options = new FirestoreRecyclerOptions
                .Builder<Material>()
                .setQuery(query, Material.class).build();

        mAdapter = new MaterialAdapter(options);
        mRecycler.setAdapter(mAdapter);

        //Popup confirm-----------------------------------------------------------------------

        View popupView = getLayoutInflater().inflate(R.layout.confirm_popup, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setAnimationStyle(androidx.appcompat.R.style.Animation_AppCompat_Dialog);

        Button yes = popupView.findViewById(R.id.yes);
        Button no = popupView.findViewById(R.id.no);

        yes.setOnClickListener(v -> {
            CollectionReference materialsRef = db.collection("materials");
            Query query_isSelected = materialsRef.whereEqualTo("isSelected", true);
            query_isSelected.get().addOnCompleteListener(task -> {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    materialsRef.document(document.getId()).update("isSelected", false);
                }
            });

            Query query_materialAmount = materialsRef.whereNotEqualTo("materialAmount", 0);
            query_materialAmount.get().addOnCompleteListener(task -> {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    materialsRef.document(document.getId()).update("materialAmount", 0);
                }
            });

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            cards fragmentCards = new cards();
            fragmentTransaction.replace(R.id.container, fragmentCards);
            fragmentTransaction.commit();

            Toast.makeText(getContext(), "Cargando...", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();

        });

        no.setOnClickListener(v -> popupWindow.dismiss());

        reload.setOnClickListener(v -> popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0));

        calculate.setOnClickListener(v -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference materialsRef = db.collection("materials");
            Query queryAmount = materialsRef.whereNotEqualTo("materialAmount", 0);
            queryAmount.get().addOnCompleteListener(task -> {
                double[] arr_price = new double[20];
                double[] arr_amount = new double[20];
                int index = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    sprice = document.getString("materialPrice");
                    price = Double.parseDouble(sprice);
                    amount = document.getLong("materialAmount").intValue();
                    arr_price[index] = price;
                    arr_amount[index] = amount;
                    index++;
                }
                total = 0;
                for (int i = 0; i < arr_price.length; i++) {
                    total += arr_price[i] * arr_amount[i];
                }
            });

            total fragmentTotal = new total();

            Bundle bundle = new Bundle();
            bundle.putDouble("total", total);
            fragmentTotal.setArguments(bundle);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragmentTotal);
            fragmentTransaction.commit();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}