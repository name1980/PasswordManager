package com.security.passwordmanager.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.security.passwordmanager.BottomSheet;
import com.security.passwordmanager.R;
import com.security.passwordmanager.SettingsActivity;
import com.security.passwordmanager.data.BankCard;
import com.security.passwordmanager.data.Data;
import com.security.passwordmanager.data.DataViewModel;
import com.security.passwordmanager.data.Website;
import com.security.passwordmanager.settings.Support;
import com.security.passwordmanager.ui.account.PasswordActivity;
import com.security.passwordmanager.ui.bank.BankCardActivity;

import java.util.List;

public class PasswordListFragment extends Fragment {

    private static final String OPENED_VIEW_KEY = "OPENED_VIEW_KEY";

    private RecyclerView mRecyclerView;
    private PasswordAdapter mAdapter;
    private SearchView searchView;

    private int openedView;

    private Support mSupport;
    private DataViewModel mDataViewModel;
    private List<Data> dataList;

    private BottomSheet mBottomSheet;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_password_list, container, false);
        mRecyclerView = root.findViewById(R.id.main_recycler_view);

        mBottomSheet = new BottomSheet(requireContext(), root);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        searchView = (SearchView) searchItem.getActionView();
        drawSearchView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                dataList = mDataViewModel.searchData(newText);
                openedView = dataList.size() == 1 ? 0 : -1;
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_settings)
            startActivity(SettingsActivity.newIntent(getActivity(), true));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSupport = Support.getInstance(getActivity());
        mDataViewModel = new ViewModelProvider(this).get(DataViewModel.class);

        if (savedInstanceState != null)
            openedView = savedInstanceState.getInt(OPENED_VIEW_KEY);
        else
            openedView = -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(OPENED_VIEW_KEY, openedView);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        dataList = mDataViewModel.getDataList();

        if (mAdapter == null) {
            mAdapter = new PasswordAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }
        else mAdapter.notifyDataSetChanged();
    }

    private void drawSearchView() {
        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        ImageView searchImage = searchView.findViewById(androidx.appcompat.R.id.search_button);
        ImageView clearView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);

        searchText.setTextColor(Color.WHITE);
        searchText.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        searchImage.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        clearView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }



    private class PasswordAdapter extends RecyclerView.Adapter<PasswordHolder> {

        private static final int OPENED_VIEW_TYPE = 0xAAA;

        @Override
        public int getItemViewType(int position) {
            if (position == openedView)
                return OPENED_VIEW_TYPE;
            return super.getItemViewType(position);
        }

        @NonNull
        @Override
        public PasswordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater
                    .inflate(R.layout.list_item_main_text_view, parent, false);

            RecyclerView infoRecyclerView = view.findViewById(R.id.list_item_recycler_view_more_info);

            if (viewType == OPENED_VIEW_TYPE)
                infoRecyclerView.setVisibility(View.VISIBLE);
            else
                infoRecyclerView.setVisibility(View.GONE);

            return new PasswordHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PasswordHolder holder, int position) {
            Data data = dataList.get(position);
            List<Data> accountList;

            if (dataList.size() == 1)
                accountList = dataList;
            else {
                String key;
                @Data.DataType int type;

                if (data.isWebsite()) {
                    key = ((Website) data).getAddress();
                    type = Data.TYPE_WEBSITE;
                } else {
                    key = ((BankCard) data).getBankName();
                    type = Data.TYPE_BANK_CARD;
                }

                accountList = mDataViewModel.getAccountList(key, type);
            }

            holder.bindPassword(accountList, position == openedView);

            holder.setOnClickListener(v -> {
                if (openedView == holder.getAdapterPosition())
                    openedView = -1;
                else {
                    int oldOpen = openedView;
                    openedView = holder.getAdapterPosition();
                    notifyItemChanged(oldOpen);
                }
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }


    private class PasswordHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final RecyclerView recyclerView;
        private final LinearLayout mainView;
        private final ImageView imageView;
        private final TextView textView_name, textView_subtitle;
        private final Button button_more;
        private final MotionLayout motionLayout;

        private List<Data> mAccountList;

        public PasswordHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView.findViewById(R.id.list_item_main_view);
            imageView = itemView.findViewById(R.id.list_item_image_view);
            textView_name = itemView.findViewById(R.id.list_item_text_view_name);
            textView_subtitle = itemView.findViewById(R.id.list_item_text_view_subtitle);
            button_more = itemView.findViewById(R.id.list_item_button_more);
            recyclerView = itemView.findViewById(R.id.list_item_recycler_view_more_info);

            button_more.setOnClickListener(this);
            motionLayout = itemView.findViewById(R.id.motion_container);
        }

        public void bindPassword(List<Data> accountList, boolean isShown) {
            this.mAccountList = accountList;

            recyclerView.setAdapter(new MoreInfoAdapter(mAccountList));

            updateArrow(isShown);
            if (accountList.get(0).isWebsite()) {
                textView_name.setText(((Website) accountList.get(0)).getNameWebsite());
                textView_subtitle.setText(((Website) accountList.get(0)).getAddress());
            }
            else {
                textView_name.setText(((BankCard) accountList.get(0)).getBankName());
                textView_subtitle.setText(((BankCard) accountList.get(0)).getCardNumber());
            }

            imageView.setImageTintList(ColorStateList.valueOf(mSupport.getHeaderColor()));
            textView_name.setBackgroundColor(mSupport.getBackgroundColor());
            textView_subtitle.setBackgroundColor(mSupport.getBackgroundColor());
            button_more.setBackgroundTintList(ColorStateList.valueOf(mSupport.getFontColor()));

            textView_name.setTextColor(mSupport.getFontColor());
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {

            mBottomSheet.setHeading(
                    ((Website) mAccountList.get(0)).getNameWebsite(),
                    ((Website) mAccountList.get(0)).getAddress()
            );

            mBottomSheet.updateImageAndText(
                    new int[]{
                            R.string.edit_password,
                            R.string.copy_info,
                            R.string.delete_password
                    },
                    new int[]{
                            R.drawable.ic_outline_edit_24,
                            R.drawable.ic_baseline_content_copy_24,
                            R.drawable.ic_baseline_delete_24
                    }
            );

            mBottomSheet.setOnClickListener(BottomSheet.VIEW_EDIT, v1 -> {
                if (mAccountList.get(0).isWebsite())
                    startActivity(PasswordActivity.newIntent(
                            requireContext(),
                            ((Website) mAccountList.get(0)).getAddress()
                    ));
                else
                    startActivity(BankCardActivity.getIntent(
                            requireContext(),
                            ((BankCard) mAccountList.get(0)).getBankName()
                    ));
                mBottomSheet.stop();
            });

            mBottomSheet.setOnClickListener(BottomSheet.VIEW_COPY, view -> {
                mDataViewModel.copyAccountList(mAccountList);
                mBottomSheet.stop();
            });

            mBottomSheet.setOnClickListener(BottomSheet.VIEW_DELETE, v1 -> {
                Data data = mAccountList.get(0);
                if (data.isBankCard())
                    mDataViewModel.deleteData(((BankCard) data).getBankName(), Data.TYPE_BANK_CARD);
                else
                    mDataViewModel.deleteData(((Website) data).getAddress(), Data.TYPE_WEBSITE);
                mBottomSheet.stop();
                updateUI();
            });

            mBottomSheet.start();
        }

        public void setOnClickListener(View.OnClickListener listener) {
            mainView.setOnClickListener(listener);
        }


        //true - стрелка вниз, false - вправо
        private void updateArrow(boolean isShown) {
            if (isShown)
                motionLayout.transitionToEnd();
            else
                motionLayout.transitionToStart();
        }
    }



    private class MoreInfoAdapter extends RecyclerView.Adapter<MoreInfoHolder> {

        private final List<Data> accountList;

        public MoreInfoAdapter(List<Data> accountList) {
            this.accountList = accountList;
        }

        @NonNull
        @Override
        public MoreInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            @LayoutRes int resId = R.layout.list_item_more_website;

            if (accountList.get(0).isBankCard())
                resId = R.layout.list_item_more_bank_card;

            View view = LayoutInflater.from(getActivity())
                    .inflate(resId, parent, false);
            return new MoreInfoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MoreInfoHolder holder, int position) {
            Data data = accountList.get(position);
            holder.bindInfo((Website) data);
        }

        @Override
        public int getItemCount() {
            return accountList.size();
        }
    }



    private class MoreInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final LinearLayout moreInfo;
        private final TextView accountName;
        private final ConstraintLayout login, password, comment;
        private final TextView head_login, head_password, head_comment;
        private final Button button_open_url;
        private Website data;

        private boolean isPasswordVisible;

        public MoreInfoHolder(@NonNull View itemView) {
            super(itemView);
            moreInfo = itemView.findViewById(R.id.list_item_linear_layout_more);

            accountName = itemView.findViewById(R.id.list_item_text_view_name_of_account);
            login = itemView.findViewById(R.id.list_item_text_view_login);
            password = itemView.findViewById(R.id.list_item_text_view_password);
            comment = itemView.findViewById(R.id.list_item_text_view_comment);
            button_open_url = itemView.findViewById(R.id.list_item_button_open_url);

            button_open_url.setOnClickListener(this);

            head_login = itemView.findViewById(R.id.list_item_text_view_login_head);
            head_password = itemView.findViewById(R.id.list_item_text_view_password_head);
            head_comment = itemView.findViewById(R.id.list_item_text_view_comment_head);

            button_open_url.setTextColor(Color.WHITE);

            isPasswordVisible = false;
        }

        public void bindInfo(Website data) {
            this.data = data;

            if (data.getComment().length() == 0) {
                comment.setVisibility(View.GONE);
                head_comment.setVisibility(View.GONE);
            }
            if (data.getNameAccount().length() == 0)
                accountName.setVisibility(View.GONE);
            else
                accountName.setText(data.getNameAccount());

            setTextToLayout(login, data.getLogin(), null);
            setTextToLayout(password, data.getPassword(), 129);
            setTextToLayout(comment, data.getComment(), null);

            setOnClickListener(login, data.getLogin(), false);
            setOnClickListener(password, data.getPassword(), true);
            setOnClickListener(comment, data.getComment(), false);

            moreInfo.setBackgroundColor(mSupport.getLayoutBackgroundColor());
            accountName.setTextColor(mSupport.getDarkerGrayColor());
            accountName.setBackgroundColor(mSupport.getLayoutBackgroundColor());
            setColor(login);
            setColor(password);
            setColor(comment);
            button_open_url.setBackgroundResource(mSupport.getButtonRes());

            head_login.setTextColor(mSupport.getFontColor());
            head_password.setTextColor(mSupport.getFontColor());
            head_comment.setTextColor(mSupport.getFontColor());
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.list_item_button_open_url) {
                String address;
                if (data.getAddress().contains("www."))
                    address = "https://" + data.getAddress();
                else if (data.getAddress().contains("https://www.") || data.getAddress().contains("http://www."))
                    address = data.getAddress();
                else
                    address = "https://www." + data.getAddress();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                startActivity(intent);
            }
        }

        private void setTextToLayout(ConstraintLayout layout, String text, Integer inputType) {
            TextView textView = layout.findViewById(R.id.field_item_text_view);
            textView.setText(text);
            if (inputType != null)
                textView.setInputType(inputType);
        }

        private void setOnClickListener(ConstraintLayout layout, String text, boolean needVisibility) {
            ImageButton copy = layout.findViewById(R.id.field_item_button_copy);
            ImageButton visibility = layout.findViewById(R.id.field_item_button_visibility);

            copy.setOnClickListener(v ->
                    mDataViewModel.copyText(text));

            if (needVisibility) {
                visibility.setVisibility(View.VISIBLE);
                visibility.setOnClickListener(v -> {
                    TextView textView = layout.findViewById(R.id.field_item_text_view);

                    isPasswordVisible = !isPasswordVisible;

                    if (isPasswordVisible) {
                        textView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        visibility.setImageResource(R.drawable.ic_outline_visibility_off_24);
                    }
                    else {
                        textView.setInputType(129);
                        visibility.setImageResource(R.drawable.ic_visibility_24);
                    }
                });
            }
            else visibility.setVisibility(View.GONE);
        }

        private void setColor(ConstraintLayout layout) {
            TextView textView = layout.findViewById(R.id.field_item_text_view);
            ImageButton copy = layout.findViewById(R.id.field_item_button_copy);
            ImageButton visibility = layout.findViewById(R.id.field_item_button_visibility);

            textView.setTextColor(mSupport.getFontColor());
            copy.setImageTintList(ColorStateList.valueOf(mSupport.getFontColor()));
            visibility.setImageTintList(ColorStateList.valueOf(mSupport.getFontColor()));

            textView.setBackgroundColor(mSupport.getLayoutBackgroundColor());
            copy.setBackgroundColor(mSupport.getLayoutBackgroundColor());
            visibility.setBackgroundColor(mSupport.getLayoutBackgroundColor());
        }
    }
}