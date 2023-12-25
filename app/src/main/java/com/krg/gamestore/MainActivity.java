package com.krg.gamestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.krg.gamestore.Models.AppItemAdapter;
import com.krg.gamestore.Models.AppModel;
import com.krg.gamestore.Models.Game;
import com.krg.gamestore.Models.GameAdapter;
import com.krg.gamestore.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.cert.CertStoreException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.Manifest;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    /*private RecyclerView recyclerView;
    private AppItemAdapter adapter;
    private List<AppModel> appList;*/

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Game> gameList;
    private  List<Game> ShowingGameList;
    private GameAdapter gameAdapter;
    private DatabaseReference databaseReference;
    private SearchView searchView;
    TextView GameTypeShowing;

    private static final int ITEMS_PER_PAGE = 50;
    private int CurrpageNum = 1;
    private boolean isLoading = false;
    boolean popupShown;

    FirebaseAuth auth;
    FirebaseUser user;
    private AdView adView;

    private static final int PERMISSION = 1;

    private AppUpdateManager appUpdateManager;

    boolean IsSearching = false;
    boolean IsSorted = false;
    private static final int MY_REQUEST_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        popupShown = preferences.getBoolean("popup_shown", false);

        if (isLoggedIn) {
            // User is already logged in, navigate to the main activity
            String email = preferences.getString("email", "");
            String userName = preferences.getString("username", "");

            TextView usernameTextView = findViewById(R.id.userNameText);
            if(userName!="") {
                usernameTextView.setText("Hello " + userName);
            }else{
                usernameTextView.setText("Hello " + user.getEmail());
            }
        }

        //retrieveAppData();
        recyclerView = findViewById(R.id.recycleView);
        swipeRefreshLayout = findViewById(R.id.Swipelayout);
        gameList = new ArrayList<>();
        gameAdapter = new GameAdapter(gameList, this);

        GameTypeShowing = findViewById(R.id.GameTypeShowing);
        //databaseReference = FirebaseDatabase.getInstance().getReference().child("games");

        // Initialize RecyclerView and set the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(gameAdapter);


        fetchDataFromRealtimeDatabase();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                adView = findViewById(R.id.BannerAdView);

                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);

                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle ad loading failure
                        Log.e("AdMob", "Ad failed to load: " + loadAdError.getMessage());
                    }
                });
            }
        });

        appUpdateManager = AppUpdateManagerFactory.create(this);

        // Check for updates when the activity is created or resumed
        checkForUpdates();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if the user has scrolled to the end
                if (!recyclerView.canScrollVertically(1)) {
                    // Load the next page
                    //LoadGames();
                    if(FilterApplied){
                        LoadMoreFilteredGame(SelectedFilterTag);
                    }else{
                        if(!IsSearching && !IsSorted) {
                            LoadMoreGame();
                        }
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Handle the refresh action
                if(!IsSearching && !IsSorted && !FilterApplied) {
                    refreshGames();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "blash", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    gameAdapter.setFilteredList(ShowingGameList);
                    searchView.clearFocus();
                    GameTypeShowing.setText("Games For You");
                    IsSearching = false;
                } else {
                    IsSearching = true;
                    filterData(newText);
                    //filterDataFromDatabase(newText);
                }
                searchView.requestFocus();
                return true;
            }
        });

        Button FilterButton = findViewById(R.id.btnFilter);
        FilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });

        Button SortFilterButton = findViewById(R.id.SortFilter);
        SortFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortFilterDialog();
            }
        });

        Button ClearFilterButton = findViewById(R.id.ClearFilter);
        ClearFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearFilter();
            }
        });

        ImageView ProfileButtonImage = findViewById(R.id.profileButton);
        ProfileButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        ImageView HomeImageButton = findViewById(R.id.HomeButton);
        HomeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
    }

    void refreshGames(){
        ShowingGameList.clear();
        Random random = new Random();
        int randomStartIndex = random.nextInt(6000);

        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        Set<String> savedTagsSet = preferences.getStringSet("selectedTags", null);
        List<String> savedTagsList = new ArrayList<>(savedTagsSet);

        int ind = 0;
        for(int i=randomStartIndex;i<gameList.size();i++){
            Game game = gameList.get(i);
            if (game != null) {
                String gameCategory = gameList.get(i).getCategory();

                if (gameCategory != null && savedTagsList.contains(gameCategory)) {
                    ShowingGameList.add(gameList.get(i));
                    ind++;
                }
            }
            if(ind>=ITEMS_PER_PAGE)break;
        }

        gameAdapter.setFilteredList(ShowingGameList);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void checkForUpdates() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                // Request the update by showing the update dialog
                showUpdateDialog();
            }
        });
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("A new version of the app is available. Do you want to update now?")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Start the update flow when the user clicks "Update"
                        startUpdateFlow();
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the case when the user clicks "Later"
                        dialog.dismiss();
                    }
                })
                .setCancelable(false) // Prevent the user from dismissing the dialog by tapping outside of it
                .show();
    }

    private void startUpdateFlow() {

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.krg.gamestore"));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // If the Play Store app is not available, open the Play Store website
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.krg.gamestore"));
            startActivity(intent);
        }
        // Ensure that appUpdateInfo is not null (you should have obtained it earlier)
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkForUpdates();
        // Clear focus when the activity is resumed
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    void ClearFilter(){
        if(FilterApplied) {
            View view = findViewById(R.id.ClearFilter);
            view.setVisibility(View.INVISIBLE);
            FilterApplied = false;
            SelectedFilterGames.clear();
            GameTypeShowing.setText("Games For You");
            recyclerView.scrollToPosition(0);
            gameAdapter.setFilteredList(ShowingGameList);
            IsSorted = false;
        }else{
            View view = findViewById(R.id.ClearFilter);
            view.setVisibility(View.INVISIBLE);
            GameTypeShowing.setText("Games For You");
            recyclerView.scrollToPosition(0);
            gameAdapter.setFilteredList(ShowingGameList);
            IsSorted = false;
        }
    }

    String SelectedFilterTag;
    private void showFilterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View filterView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(filterView);

        RadioGroup radioGroupTags = filterView.findViewById(R.id.radioGroupTags);
        Button btnApplyFilter = filterView.findViewById(R.id.btnApplyFilter);

        AlertDialog dialog = builder.create();
        dialog.show();
        // Add RadioButton elements to the RadioGroup for each tag
        // Add more RadioButton elements for other tags

        // Handle Apply Filter button click
        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRadioButtonId = radioGroupTags.getCheckedRadioButtonId();
                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = filterView.findViewById(selectedRadioButtonId);
                    SelectedFilterTag = selectedRadioButton.getText().toString();

                    // Apply the filter based on the selected tag
                    applyFilter(SelectedFilterTag);

                    // Dismiss the dialog
                    dialog.dismiss();
                } else {
                    // Inform the user to select a tag
                    Toast.makeText(MainActivity.this, "Please select a tag", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Show the filter dialog
        //builder.create().show();

    }

    private void showSortFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View filterView = getLayoutInflater().inflate(R.layout.dialog_filtersort, null);
        builder.setView(filterView);

        RadioGroup radioGroupTags = filterView.findViewById(R.id.radioGroupTags);
        Button btnApplyFilter = filterView.findViewById(R.id.btnApplyFilter);

        AlertDialog dialog = builder.create();
        dialog.show();
        // Add RadioButton elements to the RadioGroup for each tag
        // Add more RadioButton elements for other tags

        // Handle Apply Filter button click
        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRadioButtonId = radioGroupTags.getCheckedRadioButtonId();
                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = filterView.findViewById(selectedRadioButtonId);
                    String SelectedSortFilter = selectedRadioButton.getText().toString();

                    // Apply the filter based on the selected tag
                    applySortFilter(SelectedSortFilter);
                    //applyFilter(SelectedFilterTag);

                    // Dismiss the dialog
                    dialog.dismiss();
                } else {
                    // Inform the user to select a tag
                    Toast.makeText(MainActivity.this, "Please select a sort filter", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Show the filter dialog
        //builder.create().show();

    }

    boolean FilterApplied = false;

    private void applyFilter(String selectedTag) {
        SelectedFilterGames.clear();
        FilterApplied = true;
        View view = findViewById(R.id.ClearFilter);
        view.setVisibility(View.VISIBLE);
        recyclerView.scrollToPosition(0);
        int ind = 0;

        for (int i = 0; i < gameList.size(); i++) {
            Game game = gameList.get(i);

            if (game != null) {
                String gameCategory = game.getCategory();
                if (gameCategory != null&&selectedTag.contains(gameCategory)) {
                    SelectedFilterGames.add(game);
                    ind++;
                }
            }

            if (ind >= ITEMS_PER_PAGE) {
                break;
            }
        }

        //Collections.shuffle(SelectedFilterGames);
        GameTypeShowing.setText(selectedTag + " Games");
        gameAdapter.setFilteredList(SelectedFilterGames);
        //Log.d("TAG", "Filter applied: " + selectedTag);
    }
    List<Game> SelectedFilterGames = new ArrayList<>();
    void applySortFilter(String selectedSortFilter){
        if(FilterApplied){
            if(Objects.equals(selectedSortFilter, "Downloads")) {
                SelectedFilterGames.sort(new Comparator<Game>() {
                    public int compare(Game obj1, Game obj2) {
                        int downloads1 = parseDownloads(obj1.getDownloads());
                        int downloads2 = parseDownloads(obj2.getDownloads());
                        return Integer.compare(downloads2, downloads1);
                    }

                    private int parseDownloads(String downloads) {
                        try {
                            if (downloads != null) {
                                if (downloads.endsWith("K+Downloads")) {
                                    return (int) (Double.parseDouble(downloads.replace("K+Downloads", "")) * 1000);
                                } else if (downloads.endsWith("M+Downloads")) {
                                    return (int) (Double.parseDouble(downloads.replace("M+Downloads", "")) * 1000000);
                                } else if (downloads.endsWith("+Downloads")) {
                                    return Integer.parseInt(downloads.replace("+Downloads", ""));
                                } else {
                                    return 0; // Default value if parsing fails
                                }
                            } else {
                                Log.e("TAG", "KrgEmpty");
                            }
                            return 0;
                        } catch (NumberFormatException e) {
                            // Handle parsing errors
                            return 0; // Default value if parsing fails
                        }
                    }
                });
                gameAdapter.setFilteredList(SelectedFilterGames);
            }
            else if(Objects.equals(selectedSortFilter, "Rating")){
                SelectedFilterGames.sort(new Comparator<Game>() {
                    public int compare(Game obj1, Game obj2) {
                        if(obj1.getRating() != null && obj2.getRating() != null) {
                            return obj2.getRating().compareToIgnoreCase(obj1.getRating());
                        }else {
                            return 0;
                        }
                    }
                });
                gameAdapter.setFilteredList(SelectedFilterGames);
            }
            else if(Objects.equals(selectedSortFilter, "Release")){
                SelectedFilterGames.sort(new Comparator<Game>() {
                    public int compare(Game obj1, Game obj2) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

                        try {
                            if(obj1.getReleased()!=null && obj2.getReleased()!=null) {
                                Date date1 = dateFormat.parse(obj1.getReleased());
                                Date date2 = dateFormat.parse(obj2.getReleased());
                                // Sorting in descending order based on release date
                                return date2.compareTo(date1);
                            }else{
                                return 0;
                            }
                        } catch (ParseException e) {
                            // Handle parsing errors
                            e.printStackTrace();
                            return 0; // Default value if parsing fails
                        }
                    }
                });
                gameAdapter.setFilteredList(SelectedFilterGames);
            }
            else if(Objects.equals(selectedSortFilter, "Top 50")){
                Toast.makeText(MainActivity.this, "Clear the filter first", Toast.LENGTH_SHORT).show();
            }
            else if(Objects.equals(selectedSortFilter, "100M+ Downloads")){
                Toast.makeText(MainActivity.this, "Clear the filter first", Toast.LENGTH_SHORT).show();
            }
        }else{
            View view = findViewById(R.id.ClearFilter);
            view.setVisibility(View.VISIBLE);
            IsSorted = true;
            recyclerView.scrollToPosition(0);
            if(Objects.equals(selectedSortFilter, "Top 50")) {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading Top 50 Games");
                progressDialog.setCancelable(false);
                progressDialog.show();
                List<Game> Top50GameList = new ArrayList<>();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Top50Games");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    // Extract data from Realtime Database snapshot
                                    if (snapshot != null) {
                                        String appId = snapshot.child("App Id").getValue(String.class);
                                        String appName = snapshot.child("App_Name").getValue(String.class);
                                        String gameLogoUrl = snapshot.child("Logo").getValue(String.class);
                                        String gameCategory = snapshot.child("Category").getValue(String.class);
                                        String gameDescription = snapshot.child("Description").getValue(String.class);
                                        String gameRelease = snapshot.child("Released").getValue(String.class);
                                        String gameDownloads = snapshot.child("Downloads").getValue(String.class);
                                        String developerName = snapshot.child("Company").getValue(String.class);
                                        //Double ratingObject = snapshot.child("Rating").getValue(Double.class);
                                        String gameRating = snapshot.child("Rating").getValue(String.class);// (ratingObject != null) ? String.valueOf(ratingObject) : "";
                                        int recordSize = (int) dataSnapshot.getChildrenCount();
                                        String videoUrl = snapshot.child("VideoUrl").getValue(String.class);
                                        String concatenatedSS = snapshot.child("ScreenShots").getValue(String.class);
                                        String[] SSurlArray = (concatenatedSS != null) ? concatenatedSS.split(" ") : new String[0];
                                        List<String> screenshotUrls = new ArrayList<>(Arrays.asList(SSurlArray));

                                        // Create a Game object and add it to the list
                                        Game game = new Game();
                                        game.setGameName(appName);
                                        game.setGameId(appId);
                                        game.setCategory(gameCategory);
                                        game.setLogoUrl(gameLogoUrl);
                                        game.setGameDescription(gameDescription);
                                        game.setReleased(gameRelease);
                                        game.setRating(gameRating);
                                        game.setDownloads(gameDownloads);
                                        game.setDeveloperId(developerName);
                                        game.setVideoUrl(videoUrl);
                                        game.setScreenshotUrls(screenshotUrls);

                                        Top50GameList.add(game);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    progressDialog.dismiss();
                                    Log.d("TAG", "krg Error: " + e);
                                    return;
                                }
                            }
                            progressDialog.dismiss();
                            gameAdapter.setFilteredList(Top50GameList);
                            GameTypeShowing.setText("Top 50 Games");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("TAG", "Database Error: " + databaseError.getMessage());
                    }
                });
            }
            else if(Objects.equals(selectedSortFilter, "100M+ Downloads")){
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading Games With 100M+ Downloads");
                progressDialog.setCancelable(false);
                progressDialog.show();
                List<Game> Games100M = new ArrayList<>();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Games100M");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    // Extract data from Realtime Database snapshot
                                    if (snapshot != null) {
                                        String appId = snapshot.child("App Id").getValue(String.class);
                                        String appName = snapshot.child("App_Name").getValue(String.class);
                                        String gameLogoUrl = snapshot.child("Logo").getValue(String.class);
                                        String gameCategory = snapshot.child("Category").getValue(String.class);
                                        String gameDescription = snapshot.child("Description").getValue(String.class);
                                        String gameRelease = snapshot.child("Released").getValue(String.class);
                                        String gameDownloads = snapshot.child("Downloads").getValue(String.class);
                                        String developerName = snapshot.child("Company").getValue(String.class);
                                        //Double ratingObject = snapshot.child("Rating").getValue(Double.class);
                                        String gameRating = snapshot.child("Rating").getValue(String.class);// (ratingObject != null) ? String.valueOf(ratingObject) : "";
                                        int recordSize = (int) dataSnapshot.getChildrenCount();
                                        String videoUrl = snapshot.child("VideoUrl").getValue(String.class);
                                        String concatenatedSS = snapshot.child("ScreenShots").getValue(String.class);
                                        String[] SSurlArray = (concatenatedSS != null) ? concatenatedSS.split(" ") : new String[0];
                                        List<String> screenshotUrls = new ArrayList<>(Arrays.asList(SSurlArray));

                                        // Create a Game object and add it to the list
                                        Game game = new Game();
                                        game.setGameName(appName);
                                        game.setGameId(appId);
                                        game.setCategory(gameCategory);
                                        game.setLogoUrl(gameLogoUrl);
                                        game.setGameDescription(gameDescription);
                                        game.setReleased(gameRelease);
                                        game.setRating(gameRating);
                                        game.setDownloads(gameDownloads);
                                        game.setDeveloperId(developerName);
                                        game.setVideoUrl(videoUrl);
                                        game.setScreenshotUrls(screenshotUrls);

                                        Games100M.add(game);
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    progressDialog.dismiss();
                                    Log.d("TAG", "krg Error: " + e);
                                    return;
                                }
                            }

                            progressDialog.dismiss();

                            gameAdapter.setFilteredList(Games100M);
                            GameTypeShowing.setText("100M+ downloads");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("TAG", "Database Error: " + databaseError.getMessage());
                    }
                });
            }else{
                Toast.makeText(MainActivity.this, "Select a filter first", Toast.LENGTH_SHORT).show();
            }
        }
    }

    int CurrentFilterPageNum = 1;
    void LoadMoreFilteredGame(String selectedTag){
        int itemsPerPage = 10;  // Assuming a constant value, adjust as needed
        int startIndex = ITEMS_PER_PAGE + (10 * (CurrentFilterPageNum - 1));
        int ind = 0;
        for (int i = startIndex; i < gameList.size() && ind < itemsPerPage; i++) {

            Game currentGame = gameList.get(i);

            if (selectedTag.contains(currentGame.getCategory())) {
                SelectedFilterGames.add(currentGame);
                ind++;
            }
        }
        CurrentFilterPageNum++;
        gameAdapter.setFilteredList(SelectedFilterGames);
    }

    int recordSize;
    Random random = new Random();
    int randomStartIndex = random.nextInt(4000);
    void LoadMoreGame(){
        for (int i = randomStartIndex + (10 * (CurrpageNum - 1)); i < randomStartIndex + (10 * (CurrpageNum)) && i < gameList.size(); i++) {
            ShowingGameList.add(gameList.get(i));
        }
        CurrpageNum++;
        gameAdapter.setFilteredList(ShowingGameList);
    }

    void LoadMoreGameFromDataBase() {
        // Assuming you have a DatabaseReference reference initialized pointing to your games node in Firebase
        // DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference().child("games");
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("gameData");

        int itemsToLoad = ITEMS_PER_PAGE;

        // Assuming CurrpageNum is the page number you want to load
        int offset = CurrpageNum * ITEMS_PER_PAGE;

        // Query the database for the next set of games
        gamesRef.orderByKey().startAt(String.valueOf(offset)).limitToFirst(itemsToLoad)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Clear the current list
                        ShowingGameList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Assuming each child node represents a game
                            Game game = snapshot.getValue(Game.class);
                            if (game != null) {
                                ShowingGameList.add(game);
                            }
                        }

                        CurrpageNum++;

                        // Update the adapter with the new list
                        gameAdapter.setFilteredList(ShowingGameList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void showPopup() {
        // Create an AlertDialog to show the pop-up
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View customTitleView = getLayoutInflater().inflate(R.layout.custom_title_layout, null);
        builder.setCustomTitle(customTitleView);
        //builder.setTitle("Choose Games You Like");
        builder.setCancelable(false);

        // Inflate the layout for the pop-up
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);
        builder.setView(popupView);

        // Add your UI elements (ListView, buttons, etc.) and handle user input
        ListView tagListView = popupView.findViewById(R.id.tagListView);
        //String[] tags = {"Casual", "Puzzle", "RPG", "Action", "Strategy", "Simulation"};
        String[] tags = {"Action","Adventure","Arcade","Card","Casino","Casual","Puzzle","Racing","Role Playing","Simulation","Sports","Strategy","Trivia","Word"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, tags);
        tagListView.setAdapter(adapter);

        builder.setPositiveButton("Next", null);
        AlertDialog alertDialog = builder.create();

        // Set the selection limit to 3
        tagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedCount = tagListView.getCheckedItemCount();
                if (selectedCount > 3) {
                    // If more than 3 tags are selected, uncheck the last clicked item
                    tagListView.setItemChecked(position, false);
                }
            }
        });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int selectedCount = tagListView.getCheckedItemCount();

                        // Check if the user has selected exactly 3 tags
                        if (selectedCount == 3) {
                            // Process the selected tags
                            SparseBooleanArray selectedItems = tagListView.getCheckedItemPositions();
                            List<String> selectedTags = new ArrayList<>();

                            for (int i = 0; i < selectedItems.size(); i++) {
                                int position = selectedItems.keyAt(i);
                                if (selectedItems.valueAt(i)) {
                                    String selectedTag = tags[position];
                                    selectedTags.add(selectedTag);
                                    //Log.d("TAG", selectedTag + " selected");
                                }
                            }

                            saveSelectedTags(selectedTags);
                            alertDialog.dismiss();
                            showSecondPopup();
                        } else {
                            Toast.makeText(MainActivity.this, "Please select exactly 3 tags", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }
    private void showSecondPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What Would You Choose");
        builder.setCancelable(false);
        // Inflate the layout for the second popup
        View secondPopupView = getLayoutInflater().inflate(R.layout.second_popup_layout, null);
        builder.setView(secondPopupView);

        // Initialize RadioGroups and RadioButtons for each question
        RadioGroup radioGroupQuestion1 = secondPopupView.findViewById(R.id.radioGroupQuestion1);
        RadioGroup radioGroupQuestion2 = secondPopupView.findViewById(R.id.radioGroupQuestion2);
        RadioGroup radioGroupQuestion3 = secondPopupView.findViewById(R.id.radioGroupQuestion3);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve user's answers from the selected RadioButtons
                int selectedOption1Id = radioGroupQuestion1.getCheckedRadioButtonId();
                int selectedOption2Id = radioGroupQuestion2.getCheckedRadioButtonId();
                int selectedOption3Id = radioGroupQuestion3.getCheckedRadioButtonId();
                RadioButton selectedOption1 = secondPopupView.findViewById(selectedOption1Id);
                RadioButton selectedOption2 = secondPopupView.findViewById(selectedOption2Id);
                RadioButton selectedOption3 = secondPopupView.findViewById(selectedOption3Id);
                String answer1 = selectedOption1.getText().toString();
                String answer2 = selectedOption2.getText().toString();
                String answer3 = selectedOption3.getText().toString();
                //String answer1 = getSelectedOptionText(selectedOption1Id);
                //String answer2 = getSelectedOptionText(selectedOption2Id);
                //String answer3 = getSelectedOptionText(selectedOption3Id);

                // Save the answers or perform any other necessary action
                Log.d("TAG", "Answer 1: " + answer1);
                Log.d("TAG", "Answer 2: " + answer2);
                Log.d("TAG", "Answer 3: " + answer3);

                List<String> updatedTags = updateTags(answer1, answer2, answer3);
                updateSelectedTags(updatedTags);
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private List<String> updateTags(String answer1, String answer2, String answer3) {
        List<String> updatedTags = new ArrayList<>();
        // Mapping between game options and tags
        Map<String, List<String>> optionTagsMapping = new HashMap<>();
        optionTagsMapping.put("Angry Birds", Arrays.asList("Casual", "Strategy"));
        optionTagsMapping.put("Candy Crush", Arrays.asList("Casual", "Puzzle"));
        optionTagsMapping.put("Subway Surfers", Arrays.asList("Arcade", "Racing"));
        optionTagsMapping.put("Fruit Ninja", Arrays.asList("Arcade","Casual"));
        optionTagsMapping.put("PUBG", Arrays.asList("Action", "Sports","Casino"));
        optionTagsMapping.put("GTA", Arrays.asList("Action", "Adventure","Role Playing"));

        String cleanAnswer1 = getCleanAnswer(answer1);
        String cleanAnswer2 = getCleanAnswer(answer2);
        String cleanAnswer3 = getCleanAnswer(answer3);

        // Check each answer and add associated tags to the list
        if (optionTagsMapping.containsKey(cleanAnswer1)) {
            updatedTags.addAll(optionTagsMapping.get(cleanAnswer1));
        }
        if (optionTagsMapping.containsKey(cleanAnswer2)) {
            updatedTags.addAll(optionTagsMapping.get(cleanAnswer2));
        }
        if (optionTagsMapping.containsKey(cleanAnswer3)) {
            updatedTags.addAll(optionTagsMapping.get(cleanAnswer3));
        }
        // Print the updated tags (you can modify this part based on your requirement)

        return updatedTags;
    }
    private String getCleanAnswer(String answer) {
        // Remove the prefix before checking in the map
        return answer.replace("a. ", "").replace("b. ", "");
    }
    private List<Game> filterGames(List<String> selectedTags, List<Game> allGames) {
        List<Game> filteredGames = new ArrayList<>();
        int ind = 0;

        for(int i=randomStartIndex;i<allGames.size();i++){
            Game game = allGames.get(i);
            if (game != null) {
                String gameCategory = allGames.get(i).getCategory();

                if (gameCategory != null && selectedTags.contains(gameCategory)) {
                    filteredGames.add(allGames.get(i));
                    ind++;
                }
            }
            if(ind>=ITEMS_PER_PAGE)break;
        }

        /*for (Game game : allGames) {
            String gameCategory = game.getCategory();

            if (selectedTags.contains(gameCategory)) {
                filteredGames.add(game);
                ind++;
            }
            if(ind>=ITEMS_PER_PAGE)break;
        }*/

        return filteredGames;
    }

    private void saveSelectedTags(List<String> selectedTags) {
        // Save the selected tags using SharedPreferences or any other storage mechanism
        SharedPreferences prefs = getSharedPreferences("user_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert the List to a Set to store in SharedPreferences
        Set<String> tagsSet = new HashSet<>(selectedTags);
        editor.putStringSet("selectedTags", tagsSet);
        editor.apply();

        //List<Game> toShowList = filterGames(selectedTags, gameList);
        ShowingGameList = filterGames(selectedTags, gameList);
        //Collections.shuffle(toShowList);
        gameAdapter.setFilteredList(ShowingGameList);
        GameTypeShowing.setText("Games For You");
    }
    private void updateSelectedTags(List<String> updatedTags) {
        // Retrieve the existing selected tags from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_preferences", MODE_PRIVATE);
        Set<String> existingTagsSet = prefs.getStringSet("selectedTags", new HashSet<>());

        // Combine existing tags with updated tags
        Set<String> combinedTagsSet = new HashSet<>(existingTagsSet);
        combinedTagsSet.addAll(updatedTags);

        // Convert the combined Set to a List
        List<String> combinedTagsList = new ArrayList<>(combinedTagsSet);
        Log.d("TAG","list"+combinedTagsList.toString());
        // Save the combined tags back to SharedPreferences
        saveSelectedTags(combinedTagsList);

        // Optionally, update the displayed games based on the combined tags
        /*List<Game> toShowList = filterGames(combinedTagsList, gameList);
        gameAdapter.setFilteredList(toShowList);
        GameTypeShowing.setText("Games For You");*/
    }
    private void parseCsvData() {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Games...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("gameData.csv");

        File localFile;
        try {
            localFile = File.createTempFile("Data", ".csv");
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            return;
        }

        fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Activity activity = MainActivity.this; // Replace YourActivity with the name of your activity
            if (activity != null) {
                try {
                    // Read CSV file using a CSV reader library or custom logic
                    CSVReader csvReader = new CSVReader(new FileReader(localFile));
                    List<String[]> records = csvReader.readAll();
                    recordSize = records.size();
                    // Process each row of the CSV file
                    for (int i = 1; i < records.size(); i++) {
                    //for (int i = 1; i < ITEMS_PER_PAGE; i++) {
                        String[] row = records.get(i);

                        // Assuming your CSV columns are in the order: App Id, Rating, Size, ...
                        String appId = row[0]; // Adjust the index based on your CSV structure
                        String appName = row[5];
                        String gameLogoUrl = row[9];
                        String gameCategory = row[7];
                        String GameDescription = row[8];
                        String GameRating = row[1];
                        String GameDownloads = row[10];
                        String DeveloperName = row[3];
                        String VideoUrl = row[11];
                        String ScreenShotUrl = row[12];
                        List<String> screenshotUrls = new ArrayList<>();
                        screenshotUrls.add(row[12]);
                        screenshotUrls.add(row[13]);
                        screenshotUrls.add(row[14]);
                        screenshotUrls.add(row[15]);

                        // Create a Game object and add it to the list
                        Game game = new Game();
                        game.setGameName(appName);
                        game.setGameId(appId);
                        game.setCategory(gameCategory);
                        game.setLogoUrl(gameLogoUrl);
                        game.setGameDescription(GameDescription);
                        game.setRating(GameRating);
                        game.setDownloads(GameDownloads);
                        game.setDeveloperId(DeveloperName);
                        game.setVideoUrl(VideoUrl);
                        game.setSSurl(ScreenShotUrl);
                        game.setScreenshotUrls(screenshotUrls);

                        gameList.add(game);
                    }

                    localFile.delete();
                    progressDialog.dismiss();
                    //Collections.shuffle(gameList);
                    //Collections.sort(gameList, (game1, game2) -> Integer.compare(Integer.parseInt(game1.getRating()), Integer.parseInt(game2.getRating())));
                    //gameAdapter.setFilteredList(gameList);
                    //gameAdapter.notifyDataSetChanged();
                    SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                    if (!popupShown) {
                        // Show the pop-up
                        showPopup();
                        // Mark that the pop-up has been shown
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("popup_shown", true);
                        editor.apply();
                    } else {
                        Set<String> savedTagsSet = preferences.getStringSet("selectedTags", null);
                        List<String> savedTagsList = new ArrayList<>(savedTagsSet);
                        Log.d("TAG","tags are "+savedTagsList);
                        //List<Game> toShowList = filterGames(savedTagsList, gameList);
                        ShowingGameList = filterGames(savedTagsList, gameList);

                        Collections.shuffle(ShowingGameList);
                        gameAdapter.setFilteredList(ShowingGameList);
                        GameTypeShowing.setText("Games For You");
                    }
                } catch (Exception e) {
                    System.out.println("Error when loading CSV file: " + e.getMessage());
                    progressDialog.dismiss();
                }
            }

        }).addOnFailureListener(e -> {
            Toast toast = Toast.makeText(MainActivity.this, "Failed to download CSV", Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    private void fetchDataFromRealtimeDatabase() {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Games... May take a minute depending on your internet (try vpn is not loading)");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("gameData");

        databaseReference.orderByKey().limitToFirst(8000).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            // Extract data from Realtime Database snapshot
                            if (snapshot != null) {
                                String appId = snapshot.child("App Id").getValue(String.class);
                                String appName = snapshot.child("App_Name").getValue(String.class);
                                String gameLogoUrl = snapshot.child("Logo").getValue(String.class);
                                String gameCategory = snapshot.child("Category").getValue(String.class);
                                String gameDescription = snapshot.child("Description").getValue(String.class);
                                String gameRelease = snapshot.child("Released").getValue(String.class);
                                String gameDownloads = snapshot.child("Downloads").getValue(String.class);
                                String developerName = snapshot.child("Company").getValue(String.class);
                                //Double ratingObject = snapshot.child("Rating").getValue(Double.class);
                                String gameRating = snapshot.child("Rating").getValue(String.class);// (ratingObject != null) ? String.valueOf(ratingObject) : "";
                                int recordSize = (int) dataSnapshot.getChildrenCount();
                                String videoUrl = snapshot.child("VideoUrl").getValue(String.class);
                                String concatenatedSS = snapshot.child("ScreenShots").getValue(String.class);
                                String[] SSurlArray = (concatenatedSS != null) ? concatenatedSS.split(" ") : new String[0];
                                List<String> screenshotUrls = new ArrayList<>(Arrays.asList(SSurlArray));

                                // Create a Game object and add it to the list
                                Game game = new Game();
                                game.setGameName(appName);
                                game.setGameId(appId);
                                game.setCategory(gameCategory);
                                game.setLogoUrl(gameLogoUrl);
                                game.setGameDescription(gameDescription);
                                game.setReleased(gameRelease);
                                game.setRating(gameRating);
                                game.setDownloads(gameDownloads);
                                game.setDeveloperId(developerName);
                                game.setVideoUrl(videoUrl);
                                game.setScreenshotUrls(screenshotUrls);

                                gameList.add(game);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("TAG", "krg Error: " + e);
                            Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            return;
                        }
                    }

                    progressDialog.dismiss();

                    SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                    if (!popupShown) {
                        // Show the pop-up
                        showPopup();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("popup_shown", true);
                        editor.apply();
                    }
                    else {
                        Set<String> savedTagsSet = preferences.getStringSet("selectedTags", null);
                        List<String> savedTagsList = new ArrayList<>(savedTagsSet);
                        ShowingGameList = filterGames(savedTagsList, gameList);
                        Collections.shuffle(ShowingGameList);
                        gameAdapter.setFilteredList(ShowingGameList);
                        GameTypeShowing.setText("Games For You");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "Database Error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchDataFromFirestore() {
        /*ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Games...");
        progressDialog.setCancelable(false);
        progressDialog.show();*/

        ProgressBar progressBar = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                24
        ));
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(progressBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Loading Games... May take a minute depending on your internet");
        builder.setCancelable(false);
        builder.setView(layout);

        AlertDialog progressDialog = builder.create();
        progressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference gameDataCollection = db.collection("gameData");

        gameDataCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                int totalItems = (int) documents.size();
                int currentProgress = 0;
                for (DocumentSnapshot document : documents) {
                    try {
                        // Extract data from Firestore document
                        if(document != null) {
                            String appId = document.getString("App Id");
                            Object appNameObject = document.get("App_Name");
                            String appName = (appNameObject != null) ? appNameObject.toString() : null;
                            //String appName = document.getString("App_Name").toString();
                            String gameLogoUrl = document.getString("Logo");
                            String gameCategory = document.getString("Category");
                            String gameDescription = document.getString("Description");
                            String GameRelease = document.getString("Released");
                            String gameDownloads = document.getString("Downloads");
                            Object companyObject = document.get("Company");
                            String developerName = (companyObject != null) ? companyObject.toString() : null;
                            Object ratingObject = document.get("Rating");
                            String gameRating = "";
                            if (ratingObject != null) {
                                gameRating = String.valueOf(ratingObject);
                            }
                            recordSize = documents.size();
                            String videoUrl = document.getString("VideoUrl");
                            String ConcatenatedSS = document.getString("ScreenShots");
                            String[] SSurlArray = ConcatenatedSS.split(" ");
                            List<String> screenshotUrls = new ArrayList<>(Arrays.asList(SSurlArray));

                            // Create a Game object and add it to the list
                            Game game = new Game();
                            game.setGameName(appName);
                            game.setGameId(appId);
                            game.setCategory(gameCategory);
                            game.setLogoUrl(gameLogoUrl);
                            game.setGameDescription(gameDescription);
                            game.setReleased(GameRelease);
                            game.setRating(gameRating);
                            game.setDownloads(gameDownloads);
                            game.setDeveloperId(developerName);
                            game.setVideoUrl(videoUrl);
                            game.setScreenshotUrls(screenshotUrls);

                            gameList.add(game);
                            currentProgress++;
                            int progress = (int) ((currentProgress / (float) totalItems) * 100);
                            progressBar.setProgress(progress);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("TAG","krg here "+e );
                        progressDialog.dismiss();
                        return;
                    }
                }

                progressDialog.dismiss();
                // Now you have the gameList populated with data from Firestore
                // You can continue with the rest of your code
                // ...
                SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                if (!popupShown) {
                    // Show the pop-up
                    Log.d("TAG","krg PopUp Showing");

                    showPopup();
                    // Mark that the pop-up has been shown
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("popup_shown", true);
                    editor.apply();
                } else {
                    Log.d("TAG","krg PopUp Shown");
                    Set<String> savedTagsSet = preferences.getStringSet("selectedTags", null);
                    List<String> savedTagsList = new ArrayList<>(savedTagsSet);
//                    Log.d("TAG","tags are "+savedTagsList);
//                    //List<Game> toShowList = filterGames(savedTagsList, gameList);
                    ShowingGameList = filterGames(savedTagsList, gameList);
                        /*Collections.sort(ShowingGameList, new Comparator<Game>(){
                            public int compare(Game obj1, Game obj2) {
                                return obj2.getRating().compareToIgnoreCase(obj1.getRating());

                                // If ratings are equal, consider downloads
//                                if (ratingComparison == 0) {
//                                    int downloads1 = parseDownloads(obj1.getDownloads());
//                                    int downloads2 = parseDownloads(obj2.getDownloads());
//                                    Log.d("KRG", "d12 are" + downloads2 + downloads1);
//
//                                    // Exclude games with very few downloads (e.g., less than 1000)
//                                    if (downloads1 < 11000 || downloads2 < 11000) {
//                                        Log.d("KRG", "d are" + downloads2 + downloads1);
//                                        return 0;  // Treat them as equal since you want to exclude them
//                                    }
//
//                                    // Compare based on downloads
//                                    return Integer.compare(downloads2, downloads1);
//                                }

                                //return ratingComparison;
                            }

                            private int parseDownloads(String downloads) {
                                try {
                                    if(downloads!=null) {
                                        if (downloads.endsWith("K+Downloads")) {
                                            return (int) (Double.parseDouble(downloads.replace("K+Downloads", "")) * 1000);
                                        } else if (downloads.endsWith("M+Downloads")) {
                                            return (int) (Double.parseDouble(downloads.replace("M+Downloads", "")) * 1000000);
                                        } else if (downloads.endsWith("+Downloads")) {
                                            return Integer.parseInt(downloads.replace("+Downloads", ""));
                                        } else {
                                            return 0; // Default value if parsing fails
                                        }
                                    }else {
                                        Log.e("TAG","KrgEmpty");
                                    }
                                    return 0;
                                } catch (NumberFormatException e) {
                                    // Handle parsing errors
                                    return 0; // Default value if parsing fails
                                }
                            }
                        });*/
                    Collections.shuffle(ShowingGameList);
                    gameAdapter.setFilteredList(ShowingGameList);
                    GameTypeShowing.setText("Games For You");
                }

            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data from Firestore", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void parseCsvData_internal() {
        // Check if the CSV file exists in internal storage
        File internalFile = new File(getFilesDir(), "gameData.csv");
        if (internalFile.exists()) {
            // If the file exists, read data from the internal file
            readCsvData(internalFile);
            Toast.makeText(MainActivity.this,"internal",Toast.LENGTH_SHORT).show();
        } else {
            // If the file doesn't exist, download and save it to internal storage
            downloadCsvFile();
            Toast.makeText(MainActivity.this,"external",Toast.LENGTH_SHORT).show();

        }
    }

    private void readCsvData(File file) {
        try {
            // Read CSV file using a CSV reader library or custom logic
            CSVReader csvReader = new CSVReader(new FileReader(file));
            List<String[]> records = csvReader.readAll();
            recordSize = records.size();
            // Process each row of the CSV file
            // ... (rest of your existing code)
            for (int i = 1; i < records.size(); i++) {
                //for (int i = 1; i < ITEMS_PER_PAGE; i++) {
                String[] row = records.get(i);

                // Assuming your CSV columns are in the order: App Id, Rating, Size, ...
                String appId = row[0]; // Adjust the index based on your CSV structure
                String appName = row[5];
                String gameLogoUrl = row[9];
                String gameCategory = row[7];
                String GameDescription = row[8];
                String GameRating = row[1];
                String GameDownloads = row[10];
                String DeveloperName = row[3];
                String VideoUrl = row[11];
                String ScreenShotUrl = row[12];
                List<String> screenshotUrls = new ArrayList<>();
                screenshotUrls.add(row[12]);
                screenshotUrls.add(row[13]);
                screenshotUrls.add(row[14]);
                screenshotUrls.add(row[15]);

                // Create a Game object and add it to the list
                Game game = new Game();
                game.setGameName(appName);
                game.setGameId(appId);
                game.setCategory(gameCategory);
                game.setLogoUrl(gameLogoUrl);
                game.setGameDescription(GameDescription);
                game.setRating(GameRating);
                game.setDownloads(GameDownloads);
                game.setDeveloperId(DeveloperName);
                game.setVideoUrl(VideoUrl);
                game.setSSurl(ScreenShotUrl);
                game.setScreenshotUrls(screenshotUrls);

                gameList.add(game);
            }

            // Delete the temporary file
            file.delete();

            // ... (rest of your existing code)

            SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
            if (!popupShown) {
                // Show the pop-up
                showPopup();
                // Mark that the pop-up has been shown
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("popup_shown", true);
                editor.apply();
            } else {
                Set<String> savedTagsSet = preferences.getStringSet("selectedTags", null);
                List<String> savedTagsList = new ArrayList<>(savedTagsSet);
                Log.d("TAG","tags are "+savedTagsList);
                //List<Game> toShowList = filterGames(savedTagsList, gameList);
                ShowingGameList = filterGames(savedTagsList, gameList);
                Collections.shuffle(ShowingGameList);
                gameAdapter.setFilteredList(ShowingGameList);
                GameTypeShowing.setText("Games For You");
                // ... (rest of your existing code)
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void downloadCsvFile() {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Games...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("gameData.csv");

        File localFile;
        try {
            localFile = new File(getFilesDir(), "gameData.csv");
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            return;
        }

        fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            // File downloaded successfully, now read the data
            readCsvData(localFile);
            progressDialog.dismiss();
        }).addOnFailureListener(exception -> {
            // Handle failure
            progressDialog.dismiss();
            exception.printStackTrace();
        });
    }

    private void filterData(String query) {

        // Filter the original gameList based on the search query
        List<Game> filteredList = new ArrayList<>();
        if(query!=null) {
            for (Game game : gameList) {
                if(game.getGameName()!=null) {
                    if (game.getGameName().toLowerCase().contains(query.toLowerCase())) {
                        filteredList.add(game);
                    }
                }
            }
        }

        if (!filteredList.isEmpty()) {
            gameAdapter.setFilteredList(filteredList);
            GameTypeShowing.setText("Searched Games");
        }
    }

    private void filterDataFromDatabase(String query) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("gameData");

        // Perform a query to filter games directly in the database
        Query searchQuery = databaseReference.orderByChild("App_Name").startAt(query).endAt(query + "\uf8ff");

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Game> filteredList = new ArrayList<>();

                // Iterate through the search results
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String appId = snapshot.child("App Id").getValue(String.class);
                    String appName = snapshot.child("App_Name").getValue(String.class);
                    String gameLogoUrl = snapshot.child("Logo").getValue(String.class);
                    String gameCategory = snapshot.child("Category").getValue(String.class);
                    String gameDescription = snapshot.child("Description").getValue(String.class);
                    String gameRelease = snapshot.child("Released").getValue(String.class);
                    String gameDownloads = snapshot.child("Downloads").getValue(String.class);
                    String developerName = snapshot.child("Company").getValue(String.class);
                    //Double ratingObject = snapshot.child("Rating").getValue(Double.class);
                    String gameRating = snapshot.child("Rating").getValue(String.class);// (ratingObject != null) ? String.valueOf(ratingObject) : "";
                    int recordSize = (int) dataSnapshot.getChildrenCount();
                    String videoUrl = snapshot.child("VideoUrl").getValue(String.class);
                    String concatenatedSS = snapshot.child("ScreenShots").getValue(String.class);
                    String[] SSurlArray = (concatenatedSS != null) ? concatenatedSS.split(" ") : new String[0];
                    List<String> screenshotUrls = new ArrayList<>(Arrays.asList(SSurlArray));

                    // Create a Game object and add it to the list
                    Game game = new Game();
                    game.setGameName(appName);
                    game.setGameId(appId);
                    game.setCategory(gameCategory);
                    game.setLogoUrl(gameLogoUrl);
                    game.setGameDescription(gameDescription);
                    game.setReleased(gameRelease);
                    game.setRating(gameRating);
                    game.setDownloads(gameDownloads);
                    game.setDeveloperId(developerName);
                    game.setVideoUrl(videoUrl);
                    game.setScreenshotUrls(screenshotUrls);

                    filteredList.add(game);
                }

                if (!filteredList.isEmpty()) {
                    // Update the adapter and UI with the filtered list
                    gameAdapter.setFilteredList(filteredList);
                    GameTypeShowing.setText("Searched Games");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
                Log.e("TAG", "Database query error: " + databaseError.getMessage());
            }
        });
    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the operation
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}