package com.example.androidcicd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.androidcicd.movie.Movie;
import com.example.androidcicd.movie.MovieProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MovieProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockMovieCollection;

    @Mock
    private DocumentReference mockDocRef;

    private MovieProvider movieProvider;

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);

        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("movies")).thenReturn(mockMovieCollection);
        when(mockMovieCollection.document()).thenReturn(mockDocRef);
        when(mockMovieCollection.document(anyString())).thenReturn(mockDocRef);

        // Make sure we have a fresh instance for each test
        MovieProvider.setInstanceForTesting(mockFirestore);
        movieProvider = MovieProvider.getInstance(mockFirestore);
    }

    @Test
    public void testAddMovieSetsId() {
        // Movie to add
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);

        // Define teh ID we want to set for the movie
        when(mockDocRef.getId()).thenReturn(("123"));

        // 'add' movie to firestore
        movieProvider.addMovie(movie);
        assertEquals("Movie was not updated with correct id.", "123", movie.getId());

        // verify that we called the set movie in firestore
        verify(mockDocRef).set(movie);
    }

    @Test
    public void testDeleteMovie() {
        // Create movie and set our id
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");

        // Call the delete movie and verify the firebase delete method was called.
        movieProvider.deleteMovie(movie);
        verify(mockDocRef).delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForDifferentIds() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        // Set our ID to 1
        movie.setId("1");

        // Make sure the doc ref has a different ID
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error
        movieProvider.updateMovie(movie, "Another Title", "Another Genre", 2026);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForEmptyName() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error due to having an empty name
        movieProvider.updateMovie(movie, "", "Another Genre", 2026);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMovieShouldThrowErrorForDuplicateTitle() {
        // Create a first movie with title "Inception"
        Movie movie1 = new Movie("Inception", "Sci-Fi", 2010);
        when(mockDocRef.getId()).thenReturn("doc1");
        movie1.setId("doc1");
        // Seed the provider's local list to simulate existing movie data.
        movieProvider.getMovies().add(movie1);

        // Create a second movie with the same title "Inception"
        Movie movie2 = new Movie("Inception", "Action", 2011);
        // Simulate a different document id for the new movie.
        when(mockDocRef.getId()).thenReturn("doc2");

        // This should throw an exception because the title is a duplicate.
        movieProvider.addMovie(movie2);
    }
}