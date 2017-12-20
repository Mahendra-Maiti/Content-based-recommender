package org.lenskit.mooc.cbf;

import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.lang.Object;


public class TFIDFItemScorer extends AbstractItemScorer {
    private final DataAccessObject dao;
    private final TFIDFModel model;
    private final UserProfileBuilder profileBuilder;

    /**
     * Construct a new item scorer.  LensKit's dependency injector will call this constructor and
     * provide the appropriate parameters.
     *
     * @param dao The data access object, for looking up users' ratings.
     * @param m   The precomputed model containing the item tag vectors.
     * @param upb The user profile builder for building user tag profiles.
     */
    @Inject
    public TFIDFItemScorer(DataAccessObject dao, TFIDFModel m, UserProfileBuilder upb) {
        this.dao = dao;
        model = m;
        profileBuilder = upb;
    }

    /**
     * Generate item scores personalized for a particular user.  For the TFIDF scorer, this will
     * prepare a user profile and compare it to item tag vectors to produce the score.
     *
     * @param user   The user to score for.
     * @param items  A collection of item ids that should be scored.
     */
    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items){
        // Get the user's ratings
        List<Rating> ratings = dao.query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, user)
                                  .get();

        if (ratings == null) {
            // the user doesn't exist, so return an empty ResultMap
            return Results.newResultMap();
        }

        // Create a place to store the results of our score computations
        List<Result> results = new ArrayList<>();

        // Get the user's profile, which is a vector with their 'like' for each tag
        Map<String, Double> userVector = profileBuilder.makeUserProfile(ratings);
      
        for (Long item: items) {
            Map<String, Double> iv = model.getItemVector(item);

            Double sum_val=new Double(0);
            Double sq_item_val=new Double(0);
            Double utag_sq=new Double(0);

            for(Map.Entry<String,Double> entry: iv.entrySet())
            {
                String tag_name=entry.getKey();

                Double itag_rating=entry.getValue();

                if(userVector.containsKey(tag_name))
                    sum_val+=itag_rating*userVector.get(tag_name); //numerator

            }

            for(Map.Entry<String,Double> e1: iv.entrySet())
            {
                String tag_name=e1.getKey();
                Double itag_rating=e1.getValue();

                sq_item_val+=itag_rating*itag_rating;

            }

            for(Map.Entry<String,Double> e2: userVector.entrySet())
            {
                String tag_name=e2.getKey();
                Double utag_rating=e2.getValue();

                utag_sq+=utag_rating*utag_rating;

            }



            Double denom=new Double(Math.sqrt(sq_item_val)*Math.sqrt(utag_sq));
            if(denom!=0)
            {
                Double pred_val=new Double(sum_val/denom);
                Result r= Results.create(item,pred_val);
                results.add(r);
            }

        }

        return Results.newResultMap(results);
    }
}
































































