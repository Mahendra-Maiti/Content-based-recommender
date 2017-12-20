package org.lenskit.mooc.cbf;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.inject.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for computing {@linkplain TFIDFModel TF-IDF models} from item tag data.  Each item is
 * represented by a normalized TF-IDF vector.
 */
public class TFIDFModelProvider implements Provider<TFIDFModel> {
    private static final Logger logger = LoggerFactory.getLogger(TFIDFModelProvider.class);

    private final DataAccessObject dao;

    /**
     * Construct a model builder.  The {@link Inject} annotation on this constructor tells LensKit
     * that it can be used to build the model builder.
     *
     * @param dao The data access object.
     */
    @Inject
    public TFIDFModelProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * This method is where the model should actually be computed.
     * @return The TF-IDF model (a model of item tag vectors).
     */
    @Override
    public TFIDFModel get() {
        logger.info("Building TF-IDF model");

        // Create a map to accumulate document frequencies for the IDF computation
        Map<String, Double> docFreq = new HashMap<>();

        // Create a map to store the item TF vectors.
        Map<Long, Map<String, Double>> itemVectors = new HashMap<>();

        // Iterate over the items to compute each item's vector.
        LongSet items = dao.getEntityIds(CommonTypes.ITEM);

        for (long item : items) {
            // Create a work vector to accumulate this item's tag vector.
            Map<String, Double> work = new HashMap<>();

            boolean flag=true;
            for (Entity tagApplication : dao.query(TagData.ITEM_TAG_TYPE)
                                            .withAttribute(TagData.ITEM_ID, item)
                                            .get()) {
                String tag = tagApplication.get(TagData.TAG);

                if(!work.containsKey(tag))
                {
                    work.put(tag,1.0D);
                    docFreq.put(tag,docFreq.containsKey(tag)?docFreq.get(tag)+1:1);
                }
                else {
                    work.put(tag,work.get(tag)+1);
                }

            }
            itemVectors.put(item, work);

        }

        logger.info("Computed TF vectors for {} items", itemVectors.size());

        // Now we've seen all the items, so we have each item's TF vector and a global vector
        // of document frequencies.
        // Invert and log the document frequency.  We can do this in-place.
        final double logN = Math.log(items.size());
        for (Map.Entry<String, Double> e : docFreq.entrySet()) {
            e.setValue(logN - Math.log(e.getValue()));
        }

        // docFreq is a log-IDF vector.  Its values can therefore be multiplied by TF values.
        // So we can use it to apply IDF to each item vector to put it in the final model.
        // Create a map to store the final model data.
        Map<Long, Map<String, Double>> modelData = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : itemVectors.entrySet()) {
            Map<String, Double> tv = new HashMap<>(entry.getValue());


            Double sum_squares= new Double(0);
            for(Map.Entry<String, Double> e1: tv.entrySet()  )
            {
                String tag_name=e1.getKey();
                Double new_val=new Double(e1.getValue()*docFreq.get(tag_name));
                e1.setValue(new_val);
                sum_squares+=new_val*new_val;
            }
            Double norm_term=Math.sqrt(sum_squares);

            for(Map.Entry<String, Double> e2: tv.entrySet()  )
            {
                e2.setValue(e2.getValue()/norm_term); //normalizing the vector
            }
            // Normalize it by dividing each element by its Euclidean norm, which is the
            // square root of the sum of the squares of the values.
            modelData.put(entry.getKey(), tv);

        }

        return new TFIDFModel(modelData);
    }
}
