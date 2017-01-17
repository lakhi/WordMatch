package com.ai.lakhi.wordmatch;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ListView searchListView;
    private KeywordsDBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = (EditText) findViewById(R.id.search_text);
        searchListView = (ListView) findViewById(R.id.search_list);

        db = new KeywordsDBHandler(this);
        db.getAllKeywords();
    }

    protected void onStart() {
        super.onStart();
        activateSearch();
    }

    private void activateSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchTask searchNow = new SearchTask();
                searchNow.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private class SearchTask extends AsyncTask<String, Void, Boolean> {
        private List<String> bestMatches;

        @Override
        protected Boolean doInBackground(String... searchText) {
            Boolean matchesFound;
            bestMatches = db.getBestMatches(searchText[0]);

            if (bestMatches != null && !bestMatches.isEmpty()) {
                if (bestMatches.size() > 10)
                    bestMatches = bestMatches.subList(0,10);
                matchesFound = true;
            }
            else
                matchesFound = false;

            return matchesFound;
        }

        @Override
        protected void onPostExecute (Boolean matchesFound) {
            if (matchesFound)
                showMatches(bestMatches);
            else
                showEmptyView();
        }

    }

    private void showMatches(List<String> bestMatches) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                bestMatches );

        searchListView.setAdapter(arrayAdapter);
    }

    private void showEmptyView() {
        searchListView.setAdapter(null);
    }
}