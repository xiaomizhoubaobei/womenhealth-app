package top.mizhoubaobei.womenhealth.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import top.mizhoubaobei.womenhealth.data.MenstrualRecord
import top.mizhoubaobei.womenhealth.databinding.FragmentListBinding
import top.mizhoubaobei.womenhealth.ui.quickadd.QuickAddViewModel

/**
 * 列表式月经记录界面
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ListViewModel
    private lateinit var quickAddViewModel: QuickAddViewModel
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
        quickAddViewModel = ViewModelProvider(requireActivity())[QuickAddViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // 初始加载第一页数据
        viewModel.loadRecords(0)
        
        // 监听快速添加页面的记录保存事件
        quickAddViewModel.recordSavedEvent.observe(viewLifecycleOwner) {
            viewModel.loadRecords(0) // 当快速添加页面保存记录后刷新列表
        }
    }
    
    private fun setupRecyclerView() {
        recordsAdapter = RecordsAdapter(
            onItemClick = { record ->
                // 编辑记录
                showEditDialog(record)
            },
            onDeleteClick = { record ->
                // 删除记录并刷新列表
                viewModel.deleteRecord(record)
                viewModel.loadRecords(0) // 删除后重新加载第一页
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
        
        // 添加分页加载监听
        binding.recyclerViewRecords.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                
                if (!viewModel.isLoading.value!! && lastVisibleItem >= totalItemCount - 5) {
                    viewModel.loadRecords(viewModel.currentPage.value!! + 1)
                }
            }
        })
    }
    
    private fun setupClickListeners() {
        binding.fabAddRecord.setOnClickListener {
            showAddDialog()
        }
    }
    
    private fun showAddDialog() {
        val dialog = AddRecordDialog.newInstance()
        dialog.setOnRecordSavedListener { record ->
            viewModel.loadRecords(0) // 添加记录后重新加载第一页
        }
        dialog.show(parentFragmentManager, "AddRecordDialog")
    }
    
    private fun showEditDialog(record: MenstrualRecord) {
        val dialog = AddRecordDialog.newInstance(record)
        dialog.setOnRecordSavedListener { updatedRecord ->
            viewModel.loadRecords(0) // 添加记录后重新加载第一页
        }
        dialog.show(parentFragmentManager, "EditRecordDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}