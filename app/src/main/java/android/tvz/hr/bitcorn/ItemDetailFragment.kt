package android.tvz.hr.bitcorn

import android.net.Uri
import android.os.Bundle
import android.tvz.hr.bitcorn.databinding.FragmentItemDetailBinding
import android.tvz.hr.bitcorn.db.Coin
import android.tvz.hr.bitcorn.retrofit.CoinServiceInterface
import android.tvz.hr.bitcorn.retrofit.ServiceGenerator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.CollapsingToolbarLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    private var coin: Coin? = null
    val API_URL = "http://10.0.2.2:8888"

    lateinit var itemPrice: TextView
    lateinit var itemPriceNote: TextView
    lateinit var coinImage: ImageView
    private var toolbarLayout: CollapsingToolbarLayout? = null

    private var _binding: FragmentItemDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        Fresco.initialize(context);
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the placeholder content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.

                val coinId = requireArguments().getString(ARG_ITEM_ID) ?: return
                fetchCoins(coinId)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        toolbarLayout = binding.toolbarLayout
        itemPrice = binding.itemPrice
        itemPriceNote = binding.itemPriceNote
        coinImage = binding.coinImage!!

        return rootView
    }

    private fun fetchCoins(coinId: String) {
        val client: CoinServiceInterface = ServiceGenerator().createService(
            CoinServiceInterface::class.java,
            API_URL
        )

        val fetchedCoin: Call<Coin> = client.fetchCoin(coinId)

        fetchedCoin.enqueue(object : Callback<Coin> {
            override fun onResponse(
                call: Call<Coin>,
                response: Response<Coin>
            ) {
                if (response.isSuccessful) {
                    coin = response.body()!!
                    updateContent()
                }
            }

            override fun onFailure(call: Call<Coin>, t: Throwable) {
                println(t.message)
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun formatPrice(price: Double): String {
        val df = DecimalFormat("#,###.#####")
        val formatted = df.format(price)
        return "$$formatted"
    }


    private fun formatPercentage(change: Double): String {
        var indicator = ""
        if (change > 0) {
            indicator = "+"
        }
        val df = DecimalFormat("#.##")
        val percentage = df.format(change)
        return "$indicator$percentage%"
    }

    private fun colorPrice(textView: TextView?, change: Double) {
        var changeColor: Int? = null;
        if (change > 0) {
            changeColor = R.color.green
        } else if (change < 0) {
            changeColor = R.color.red
        }
        if (changeColor !== null) {
            textView!!.setTextColor(
                ContextCompat.getColor(
                    textView!!.context,
                    changeColor
                )
            )
        }
    }

    private fun setPriceNoteText(textView: TextView?, change: Double) {
        val percent = formatPercentage(change)
        if (change > 0) {
            textView!!.text = "Up in the last 24 hours ($percent)"
        } else if (change < 0) {
            textView!!.text = "Down in the last 24 hours ($percent)"
        } else {
            textView!!.text = "Unchaged in the last 24 hours"
        }

    }


    private fun updateContent() {
        toolbarLayout?.title = coin?.name

        // Show the placeholder content as text in a TextView.
        coin?.let {
            itemPrice.text = formatPrice(it.current_price)
            colorPrice(itemPriceNote, it.price_change_24h)
            setPriceNoteText(itemPriceNote, it.price_change_percentage_24h)

            val uri = Uri.parse(it?.image)
            val draweeView = coinImage as SimpleDraweeView
            draweeView.setImageURI(uri)
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}