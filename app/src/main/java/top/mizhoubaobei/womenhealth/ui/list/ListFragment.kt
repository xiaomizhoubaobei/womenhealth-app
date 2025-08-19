package top.mizhoubaobei.womenhealth.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.databinding.FragmentListBinding

/**
 * 列表式月经记录界面
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ListViewModel
    private lateinit var recordsAdapter: RecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[ListViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // 加载数据
        viewModel.loadAllRecords()
    }
    
    private fun setupRecyclerView() {
        recordsAdapter = RecordsAdapter(
            onItemClick = { record ->
                // 编辑记录
                showEditDialog(record)
            },
            onDeleteClick = { record ->
                // 删除记录
                viewModel.deleteRecord(record)
            }
        )
        
        binding.recyclerViewRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.allRecords.observe(viewLifecycleOwner) { records ->
            recordsAdapter.updateRecords(records)
            
            // 显示/隐藏空状态
            if (records.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewRecords.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewRecords.visibility = View.VISIBLE
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddRecord.setOnClickListener {
            showAddDialog()
        }
    }
    
    private fun showAddDialog() {
        val dialog = AddRecordDialog.newInstance()
        dialog.setOnRecordSavedListener { record ->
            viewModel.loadAllRecords()
        }
        dialog.show(parentFragmentManager, "AddRecordDialog")
    }
    
    private fun showEditDialog(record: MenstrualRecord) {
        val dialog = AddRecordDialog.newInstance(record)
        dialog.setOnRecordSavedListener { updatedRecord ->
            viewModel.loadAllRecords()
        }
        dialog.show(parentFragmentManager, "EditRecordDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}