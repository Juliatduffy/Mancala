package com.example.mancala

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mancala.databinding.FragmentGameBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameFragment : Fragment(R.layout.fragment_game) {
    private val viewModel: GameViewModel by viewModels()
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var holes: List<FrameLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameBinding.bind(view)

        holes = listOf(
            binding.pit0,
            binding.pit1,
            binding.pit2,
            binding.pit3,
            binding.pit4,
            binding.pit5,
            binding.pit6,
            binding.pit7,
            binding.pit8,
            binding.pit9,
            binding.pit10,
            binding.pit11,
            binding.pit12,
            binding.pit13
        )

        redrawAllPits(viewModel.marbles.value)

        holes.forEachIndexed { index, container ->
            container.setOnClickListener {
                if (index == 6 || index == 13) return@setOnClickListener
                if (viewModel.moveInProgress.value) return@setOnClickListener
                val currentCounts = viewModel.marbles.value
                if (currentCounts[index] == 0) {
                    Toast.makeText(requireContext(), "Pit $index is empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.move(index)
                viewModel.logBoardState()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moveMarbleEvent.collect { (fromPit, toPit) ->
                animateSingleMarbleMove(fromPit, toPit)
                delay(300L)
            }
        }
    }

    private fun redrawAllPits(counts: List<Int>) {
        if (::holes.isInitialized.not() || counts.size < holes.size) return
        for (i in holes.indices) {
            val container = holes[i]
            container.removeAllViews()
            repeat(counts[i]) {
                val marble = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.yellow)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    translationX = Random.nextInt(-10, 10).toFloat()
                    translationY = Random.nextInt(-10, 10).toFloat()
                }
                container.addView(marble)
            }
        }
    }

    private fun animateSingleMarbleMove(fromPit: Int, toPit: Int) {
        val overlay = binding.animationOverlay
        if (fromPit !in holes.indices || toPit !in holes.indices) return
        val fromView = holes[fromPit]
        val fromLoc = IntArray(2).also { fromView.getLocationOnScreen(it) }
        val fromCenterX = fromLoc[0] + fromView.width / 2f
        val fromCenterY = fromLoc[1] + fromView.height / 2f
        val toView = holes[toPit]
        val toLoc = IntArray(2).also { toView.getLocationOnScreen(it) }
        val toCenterX = toLoc[0] + toView.width / 2f
        val toCenterY = toLoc[1] + toView.height / 2f
        val flyingMarble = ImageView(requireContext()).apply {
            setImageResource(R.drawable.yellow)
            layoutParams = FrameLayout.LayoutParams(40, 40).also { params ->
                params.leftMargin = (fromCenterX - 20).toInt()
                params.topMargin = (fromCenterY - 20).toInt()
            }
        }
        overlay.addView(flyingMarble)
        flyingMarble.animate()
            .x(toCenterX - flyingMarble.width / 2f)
            .y(toCenterY - flyingMarble.height / 2f)
            .setDuration(200L)
            .withEndAction {
                overlay.removeView(flyingMarble)
                val sourceContainer = holes[fromPit]
                if (sourceContainer.childCount > 0) {
                    sourceContainer.removeViewAt(sourceContainer.childCount - 1)
                }
                val destContainer = holes[toPit]
                val newMarble = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.yellow)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                destContainer.addView(newMarble)
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
