package com.example.funun.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.funun.Adapter.EventDetailsPagerAdapter;
import com.example.funun.R;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

import static android.content.Context.MODE_PRIVATE;


/**This Class EventDetailsFragment - Creates A New Tablayout that represent the specific event details that the user chose */
public class EventDetailsFragment extends Fragment {

    View view;
    ViewPager viewPager;
    TabLayout tabLayout;
    private boolean no_info;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        no_info=true;
    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     * this method initialize the Tablayout with fragments only if the shared pref file contains event details
     * @return Return the View for the fragment's UI, or null.
     * @param inflater - The LayoutInflater object that can be used to inflate views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmentz
        if(getActivity().getSharedPreferences("Event",MODE_PRIVATE).getAll().size()!=0) {
            view = inflater.inflate(R.layout.fragment_event_details, container, false);
            viewPager = view.findViewById(R.id.viewPager);
            tabLayout = view.findViewById(R.id.tabLayout);
            no_info=false;
        }
        else {
            view= inflater.inflate(R.layout.no_info, container, false);
            no_info=true;
        }
        return view;
    }

    /**This Method - onActivityCreated - initialize the tab layout and fragments */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(!no_info) {
            setUpViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
        }
    }
/**This Method setUpViewPager - initialize the pages (fragments) and set adapter EventDetailsPagerAdapter to the viewpager. */
    private void setUpViewPager(ViewPager viewPager) {
        EventDetailsPagerAdapter adapter = new EventDetailsPagerAdapter(getChildFragmentManager());

        adapter.addFragment(new AboutFragment(), "About");
        adapter.addFragment(new ShoppingList(),"Shop");
        adapter.addFragment(new NotesFragment(), "Notes");
        adapter.addFragment(new GuestsFragment(), "Guests");
        viewPager.setAdapter(adapter);
    }
}