# Content Based Filtering
## Overview

In this assignment, a content-based recommender is implemented as a LensKit recommender algorithm.  LensKit provides tools to produce recommendations from a user. 



The CBF recommenders are broken into three
components:

-   A model class, `TFIDFModel`.
-   A model provider, `TFIDFModelProvider`, that computes TF-IDF vectors for items.
-   A scorer/recommender class that uses the precomputed model to score items. It's only job is to compute user-personalized scores for
    items.

In addition, the item scorer uses another component, the `UserProfileBuilder`, to compute user
tag interest profiles based on a user's ratings and the tags applied to the movies they like. 


## Part 1: TF-IDF Recommender with Unweighted Profiles


Computing item-tag vectors (the model): For this task,  model builder (`TFIDFModelProvider`) is used to compute the unit-normalized TF-IDF vector for each movie in the data set. The model contains a mapping of item IDs to TF-IDF vectors, normalized to unit vectors, for each item.

 There are two main steps to this task:

 1.  Iterate through items, building the term vector  ![equation](http://latex.codecogs.com/gif.latex?%5Chat%7B%5Cmathbf%7Bq%7D_i%7D) for each item and a global document frequency vector ![equation](https://latex.codecogs.com/gif.latex?%24%5Cmathbf%7Bd%7D%24).  At this stage, these are unnormalized term and document frequency vectors, storing the number of times the term appears on each document or the number of documents in which it appears.

 2.  Iterate through each item again, performing the following:

        1.  Divide each term value ![equation](https://latex.codecogs.com/gif.latex?%5Chat%20q_%7Bit%7D) by the log of the document frequency (![equation](https://latex.codecogs.com/gif.latex?%24%5Cmathrm%7Bln%7D%20d_t%24)).  The resulting vector ![equation](https://latex.codecogs.com/gif.latex?%5Ctilde%7B%5Cmathbf%7Bq%7D_i%7D) is the TF-IDF vector.
        2.  After dividing each term value by the log of the DF, compute the length (Euclidean norm) of the TF-IDF vector ![equation](https://latex.codecogs.com/gif.latex?%5Ctilde%7B%5Cmathbf%7Bq%7D_i%7D), and divide each element of it by the length to yield a unit vector ![equation](https://latex.codecogs.com/gif.latex?%24%5Cmathbf%7Bq%7D_i%24).

    The `getItemTags` method of the `ItemTagDAO` class, an instance of which is available as a field in your model builder, will give you the list of all tags applied to the item.

Building user profile for each query user:  The `UserProfileBuilder` interface defines classes that take a user's history – a list of ratings — and produce a vector ![equation](https://latex.codecogs.com/gif.latex?%24%5Cmathbf%7Bp%7D_u%24) representing that user's profile. The user profile vectors will also be term vectors, describing how much the user likes each tag.  For part 1, the profile should be the sum of the item-tag vectors of all items the user has rated positively (>= 3.5 stars); this implementation goes in `ThresholdUserProfileBuilder`.

 Mathematically, this is:

![equation](https://latex.codecogs.com/gif.latex?%24%24p_%7But%7D%20%3D%20%5Csum_%7Bi%20%5Cin%20I_u%3A%20r_%7Bui%7D%20%5Cge%203.5%7D%20q_%7Bit%7D%24%24)

Generating item scores for each user: The heart of the recommendation process in many LensKit recommenders is the score method of the item scorer, in this case `TFIDFItemScorer`. This method scores each item by using cosine similarity: the score for an item is the cosine between that item's tag vector and the user's profile vector. Cosine similarity is defined as follows:

   ![equation](https://latex.codecogs.com/gif.latex?%24%24%5Cmathrm%7Bcos%7D%28%5Cmathbf%7Bp_u%7D%2C%5Cmathbf%7Bq_i%7D%29%20%3D%20%5Cfrac%7B%5Cmathbf%7Bp_u%7D%20%5Ccdot%20%5Cmathbf%7Bq_i%7D%7D%7B%5C%7C%5Cmathbf%7Bp_u%7D%5C%7C_2%20%5C%7C%5Cmathbf%7Bq_i%7D%5C%7C_2%7D%20%3D%20%5Cfrac%7B%5Csum_t%20q_%7But%7D%20p_%7Bit%7D%7D%7B%5Csqrt%7B%5Csum_t%20q_%7But%7D%5E2%7D%20%5Csqrt%7B%5Csum_t%20p_%7Bit%7D%5E2%7D%7D%24%24)

The program is run from the command line using Gradle:

    ```
    ./gradlew recommendBasic -PuserId=320
    ```



## Part 2: Weighted User Profile

In this variant, rather than just summing the vectors for all positively-rated items, a weighted sum of the item vectors is computed for all rated items, with weights being based on the user's rating. The following formula is implemented:

![equation](https://latex.codecogs.com/gif.latex?%24%24%5Cmathbf%7Bp%7D_u%20%3D%20%5Csum_%7Bi%20%5Cin%20I%28u%29%7D%20%28r_%7Bui%7D%20-%20%5Cmu_u%29%20%5Cmathbf%7Bq%7D_i%24%24)

Using non-vector notation, each user tag value ![equation](https://latex.codecogs.com/gif.latex?%24x_%7But%7D%24) is computed as follows:

![equation](https://latex.codecogs.com/gif.latex?%24%24p_%7But%7D%20%3D%20%5Csum_%7Bi%20%5Cin%20I%28u%29%7D%20%28r_%7Bui%7D%20-%20%5Cmu_u%29%20q_%7Bit%7D%24%24)

If an item ![equation](https://latex.codecogs.com/gif.latex?%24i%24) has no data for a tag ![equation](https://latex.codecogs.com/gif.latex?%24t%24), then ![equation](https://latex.codecogs.com/gif.latex?%24q_%7Bit%7D%20%3D%200%24).

### Example Output for Weighted User Profile


## Running the Program

`recommendBasic` computes recommendations using the threshold user profile builder, and `recommendWeighted` uses the weighted user profile builder. 

For example:

```
$ ./gradlew recommendBasic -PuserId=91
```
