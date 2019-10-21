package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;

/**
 * Created by NSW00_007906 on 2017/10/11.
 */

public class RecyclerViewMatcher {
    public static Matcher<View> withIdInRecyclerView(int id, int recyclerViewId, int position) {
        return allOf(ViewMatchers.withId(id), isDescendantOfRecyclerView(recyclerViewId, position));
    }

    public static Matcher<View> isDescendantOfRecyclerView(final int recyclerViewId, final int position) {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object arg) {
                if (arg instanceof View) {
                    View v = (View) arg;
                    View parent = v;
                    while (parent.getParent() != null && parent.getParent() instanceof View) {
                        if (parent.getId() == recyclerViewId && parent instanceof RecyclerView) {
                            RecyclerView.ViewHolder holder = findContainingViewHolder((RecyclerView) parent, v);
                            if (holder != null && holder.getAdapterPosition() == position) {
                                return true;
                            }
                        }
                        parent = (View) parent.getParent();
                    }
                }
                return false;
            }



            @Override
            public void describeTo(Description description) {
                description.appendText("isDescendantOfRecyclerView(")
                        .appendValue(InstrumentationRegistry.getTargetContext().getResources().getResourceEntryName(recyclerViewId))
                        .appendText(",")
                        .appendValue(position)
                        .appendText(")");
            }
        };
    }

    /* このメソッドは最近のRecyclerViewには標準で実装されています。 */
    @Nullable
    public static RecyclerView.ViewHolder findContainingViewHolder(RecyclerView recyclerView, View view) {
        View v = view;
        while (v != null && v.getParent() instanceof View) {
            if (v.getParent() == recyclerView) {
                return recyclerView.getChildViewHolder(v);
            }
            v = (View) v.getParent();
        }
        return null;
    }
}
