package org.lenskit.mooc.cbf;

import org.lenskit.data.ratings.Rating;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build a user profile from all positive ratings.
 */
public class ThresholdUserProfileBuilder implements UserProfileBuilder {
    /**
     * The lowest rating that will be considered in the user's profile.
     */
    private static final double RATING_THRESHOLD = 3.5;

    /**
     * The tag model, to get item tag vectors.
     */
    private final TFIDFModel model;

    @Inject
    public ThresholdUserProfileBuilder(TFIDFModel m) {
        model = m;
    }

    @Override
    public Map<String, Double> makeUserProfile(@Nonnull List<Rating> ratings) {
        // Create a new vector over tags to accumulate the user profile
        Map<String,Double> profile = new HashMap<>();

        // Iterate over the user's ratings to build their profile
        for (Rating r: ratings) {
            if (r.getValue() >= RATING_THRESHOLD) {

                Map<String,Double> ItemId=model.getItemVector(r.getItemId()); //get vector of tags for that item

                for(Map.Entry<String,Double> entry: ItemId.entrySet())
                {
                    String tag_name=entry.getKey();
                    Double inc_val=entry.getValue();
                    profile.put(tag_name,profile.containsKey(tag_name)?profile.get(tag_name)+inc_val:inc_val);
                }

            }
        }

        // The profile is accumulated, return it.
        return profile;
    }
}
