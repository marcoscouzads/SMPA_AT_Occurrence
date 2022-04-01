package br.com.marcoscsouza.smpaattest.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.marcoscsouza.smpaattest.R

class DetailOccurrenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_occurrence)
        title = "detalhes "
    }
}