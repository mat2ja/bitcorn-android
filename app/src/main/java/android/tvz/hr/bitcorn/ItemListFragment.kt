package android.tvz.hr.bitcorn

import android.net.Uri
import android.os.Bundle
import android.tvz.hr.bitcorn.databinding.FragmentItemListBinding
import android.tvz.hr.bitcorn.databinding.ItemListContentBinding
import android.tvz.hr.bitcorn.db.Coin
import android.tvz.hr.bitcorn.retrofit.CoinServiceInterface
import android.tvz.hr.bitcorn.retrofit.ServiceGenerator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A Fragment representing a list of Pings. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link ItemDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null

    val API_URL = "http://10.0.2.2:8888"
    var coins: MutableList<Coin> = arrayListOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Fresco.initialize(context);
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.itemList

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.item_detail_nav_container)

        fetchCoins(recyclerView, itemDetailFragmentContainer)
    }


    private fun fetchCoins(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?
    ) {
        val client: CoinServiceInterface = ServiceGenerator().createService(
            CoinServiceInterface::class.java,
            API_URL
        )

        val fetchCoins: Call<MutableList<Coin>> = client.fetchCoins()

        fetchCoins.enqueue(object : Callback<MutableList<Coin>> {
            override fun onResponse(
                call: Call<MutableList<Coin>>,
                response: Response<MutableList<Coin>>
            ) {
                if (response.isSuccessful) {
                    coins = response.body()!!

                    setupRecyclerView(recyclerView, itemDetailFragmentContainer)
                }
            }

            override fun onFailure(call: Call<MutableList<Coin>>, t: Throwable) {
                println(t.message)
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?
    ) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(
            coins, itemDetailFragmentContainer
        )
    }

    class SimpleItemRecyclerViewAdapter(
        private val values: List<Coin>,
        private val itemDetailFragmentContainer: View?
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding =
                ItemListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)

        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]

            item.apply {
                holder.apply {

                    coinRanking.text = "#$market_cap_rank"
                    coinName.text = name
                    coinSymbol.text = symbol.uppercase()

                    val uri = Uri.parse(image)
                    val draweeView = holder.coinCardImage
                    draweeView.setImageURI(uri)
                }

            }


            with(holder.itemView) {
                tag = item
                setOnClickListener { itemView ->
                    val item = itemView.tag as Coin
                    val bundle = Bundle()
                    bundle.putString(
                        ItemDetailFragment.ARG_ITEM_ID,
                        item.id
                    )
                    if (itemDetailFragmentContainer != null) {
                        itemDetailFragmentContainer.findNavController()
                            .navigate(R.id.fragment_item_detail, bundle)
                    } else {
                        itemView.findNavController().navigate(R.id.show_item_detail, bundle)
                    }
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(binding: ItemListContentBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val coinRanking: TextView = binding.coinCardRanking
            val coinName: TextView = binding.coinCardName
            val coinSymbol: TextView = binding.coinCardSymbol
            val coinCardImage: SimpleDraweeView = binding.coinCardImage
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}