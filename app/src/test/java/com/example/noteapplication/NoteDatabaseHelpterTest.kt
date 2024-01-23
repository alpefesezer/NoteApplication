import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.noteapplication.Note
import com.example.noteapplication.NotesDatabaseHelper
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@Config(manifest=Config.NONE)
class NotesDatabaseHelperTest {

    private lateinit var dbHelper: NotesDatabaseHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dbHelper = NotesDatabaseHelper(context)
    }

    @After
    fun tearDown() {
        dbHelper.writableDatabase.close()
    }

    @Test
    fun testInsertNote() {
        val noteToInsert = Note(0, "Test Title", "Test Content")
        dbHelper.insertNote(noteToInsert)

        val insertedNote = dbHelper.getAllNotes().firstOrNull()
        assertNotNull(insertedNote)
        if (insertedNote != null) {
            assertEquals("Test Title", insertedNote.title)
        }
        if (insertedNote != null) {
            assertEquals("Test Content", insertedNote.content)
        }
    }

    @Test
    fun testGetAllNotes() {
        // Insert a sample note into the database
        val noteToInsert = Note(0, "Test Title", "Test Content")
        dbHelper.insertNote(noteToInsert)

        // Retrieve all notes from the database
        val notesList = dbHelper.getAllNotes()

        assertNotNull(notesList)
        assertTrue(notesList.isNotEmpty())
    }



    @Test
    fun testGetNoteByID() {
        // Insert a note into the database
        val noteToInsert = Note(0, "Test Title", "Test Content")
        dbHelper.insertNote(noteToInsert)

        // Retrieve the inserted note
        val retrievedNote = dbHelper.getAllNotes().firstOrNull()

        assertNotNull(retrievedNote)
        if (retrievedNote != null) {
            assertTrue(retrievedNote.id > 0)
        }
    }


    @Test
    fun testUpdateNote() {
        // Insert a note into the database
        val noteToInsert = Note(0, "Initial Title", "Initial Content")
        dbHelper.insertNote(noteToInsert)

        // Retrieve the inserted note
        val existingNote = dbHelper.getAllNotes().firstOrNull()
        assertNotNull(existingNote)

        // Update the note
        val updatedNote = Note(existingNote!!.id, "Updated Title", "Updated Content")
        dbHelper.updateNote(updatedNote)

        // Retrieve the updated note
        val retrievedUpdatedNote = dbHelper.getNoteByID(existingNote.id)
        assertNotNull(retrievedUpdatedNote)
        assertEquals("Updated Title", retrievedUpdatedNote.title)
        assertEquals("Updated Content", retrievedUpdatedNote.content)
    }



    @Test
    fun testDeleteNote() {
        // Insert a note into the database
        val noteToInsert = Note(0, "Test Title", "Test Content")
        dbHelper.insertNote(noteToInsert)
        println("Note inserted: $noteToInsert")

        // Print the list of all notes before deletion
        val allNotesBeforeDeletion = dbHelper.getAllNotes()
        println("All notes before deletion: $allNotesBeforeDeletion")

        // Retrieve the inserted note
        val noteToDelete = dbHelper.getAllNotes().firstOrNull()
        assertNotNull("Failed to retrieve note to delete", noteToDelete)

        // Print the state of the note before deletion
        println("Note before deletion: $noteToDelete")

        // Delete the note
        if (noteToDelete != null) {
            println("noteToDelete id: ${noteToDelete.id}")
            dbHelper.deleteNote(noteToDelete.id)
        }

        // Wait until the database is updated or a timeout occurs
        // Using a simple sleep for demonstration purposes. In a real scenario, a better synchronization mechanism is recommended.
        Thread.sleep(1000)

        // Retrieve the deleted note again using the correct ID
        val deletedNote = dbHelper.getAllNotes().firstOrNull { it.id == noteToDelete?.id }

        // Print the list of all notes after deletion
        val allNotesAfterDeletion = dbHelper.getAllNotes()
        println("All notes after deletion: $allNotesAfterDeletion")

        // Print the state of the deleted note
        println("Deleted note: $deletedNote")

        // Check if the deleted note has the correct properties
        assertNull(deletedNote)
    }






}
