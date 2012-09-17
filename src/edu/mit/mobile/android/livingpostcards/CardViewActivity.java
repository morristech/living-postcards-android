package edu.mit.mobile.android.livingpostcards;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.data.PrivatelyAuthorable;

public class CardViewActivity extends FragmentActivity implements
        OnCreateOptionsMenuListener, OnOptionsItemSelectedListener, LoaderCallbacks<Cursor>,
        OnPrepareOptionsMenuListener {

    private static final String[] CARD_PROJECTION = new String[] { Card._ID, Card.COL_TITLE,
            Card.COL_AUTHOR_URI, Card.COL_PRIVACY };
    private static final String TAG = CardViewActivity.class.getSimpleName();
    private Uri mCard;
    private CardViewFragment mCardViewFragment;

    private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);
    private String mUserUri;
    private boolean mIsEditable;

    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        mSherlock.setContentView(R.layout.activity_card_view);

        mSherlock.getActionBar().setHomeButtonEnabled(true);

        mCard = getIntent().getData();

        final FragmentManager fm = getSupportFragmentManager();

        final FragmentTransaction ft = fm.beginTransaction();

        final Fragment f = fm.findFragmentById(R.id.card_view_fragment);
        if (f != null) {
            mCardViewFragment = (CardViewFragment) f;
        } else {
            mCardViewFragment = CardViewFragment.newInstance(mCard);
            ft.replace(R.id.card_view_fragment, mCardViewFragment);
        }

        mUserUri = Authenticator.getUserUri(this, Authenticator.ACCOUNT_TYPE);

        getSupportLoaderManager().initLoader(0, null, this);

        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(Intent.ACTION_SYNC, mCard));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                return true;

            case android.R.id.home:
                startActivity(new Intent(Intent.ACTION_VIEW, Card.CONTENT_URI));
                return true;

            case R.id.edit:
                startActivity(new Intent(Intent.ACTION_EDIT, mCard));
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSherlock.getMenuInflater().inflate(R.menu.activity_card_view, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.edit).setVisible(mIsEditable);
        menu.findItem(R.id.delete).setVisible(mIsEditable);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return mSherlock.dispatchOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, mCard, CARD_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (c.moveToFirst()) {
            mSherlock.setTitle(c.getString(c.getColumnIndex(Card.COL_TITLE)));
            mIsEditable = PrivatelyAuthorable.canEdit(mUserUri, c);
            mSherlock.dispatchInvalidateOptionsMenu();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
