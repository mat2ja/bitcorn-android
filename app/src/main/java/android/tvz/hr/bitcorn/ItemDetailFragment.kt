package android.tvz.hr.bitcorn

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.tvz.hr.bitcorn.databinding.FragmentItemDetailBinding
import android.tvz.hr.bitcorn.db.Coin
import android.tvz.hr.bitcorn.retrofit.CoinServiceInterface
import android.tvz.hr.bitcorn.retrofit.ServiceGenerator
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.textfield.TextInputEditText
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


    lateinit var coinImage: ImageView
    lateinit var itemPrice: TextView
    lateinit var itemPriceNote: TextView
    lateinit var rank: TextView
    lateinit var marketCap: TextView
    lateinit var circSupply: TextView
    lateinit var totalSupply: TextView
    lateinit var ath: TextView
    lateinit var maxSupply: TextView
    lateinit var inputCoin: TextInputEditText
    lateinit var inputUsd: TextInputEditText

    var mediaPlayer: MediaPlayer? = null
    private var toolbarLayout: CollapsingToolbarLayout? = null
    private var _binding: FragmentItemDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        Fresco.initialize(context);
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                val coinId = requireArguments().getString(ARG_ITEM_ID) ?: return
                fetchCoins(coinId)
            }
        }

        if (savedInstanceState == null) {
            configureMedia()
        }
    }

    private fun configureMedia() {
        mediaPlayer = MediaPlayer.create(context, R.raw.kaching)
        mediaPlayer!!.isLooping = false
        mediaPlayer!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        toolbarLayout = binding.toolbarLayout
        coinImage = binding.coinImage
        rank = binding.rank
        itemPrice = binding.itemPrice
        itemPriceNote = binding.itemPriceNote
        marketCap = binding.marketCap
        circSupply = binding.circSupply
        totalSupply = binding.totalSupply
        ath = binding.ath
        maxSupply = binding.maxSupply
        inputCoin = binding.inputCoin
        inputUsd = binding.inputUsd


        binding.buttonNotify.setOnClickListener {
            sendNotification()
        }


        inputCoin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                inputUsd.setText(coin?.let { convertToUsd(s.toString(), it.current_price) })
            }
        })

        return rootView
    }

    private fun convertToUsd(amount: String, rate: Double): String {
        if (amount.isNotEmpty()) {
            val usd = (amount.toDouble()) * rate
            return formatNumber(usd)
        }
        return ""
    }

    private fun fetchCoins(coinId: String) {
        val client: CoinServiceInterface = ServiceGenerator().createService(
            CoinServiceInterface::class.java,
            API_URL
        )

        val fetchedCoin: Call<Coin> = client.fetchCoin(coinId)

        fetchedCoin.enqueue(object : Callback<Coin> {
            @RequiresApi(Build.VERSION_CODES.O)
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

    private fun formatNumber(num: Double): String {
        val df = DecimalFormat("#,###.##")
        val number = df.format(num)
        return "$number"
    }

    private fun formatNumberInt(num: Double): String {
        val df = DecimalFormat("#,###")
        val number = df.format(num)
        return "$number"
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

    private fun openWebsite() {
        val url = "https://www.coingecko.com/en/coins/${coin?.id}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                activity?.applicationContext,
                R.string.error,
                Toast.LENGTH_LONG
            ).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateContent() {
        toolbarLayout?.title = coin?.name

        // Show the placeholder content as text in a TextView.
        coin?.let {
            itemPrice.text = formatPrice(it.current_price)
            colorPrice(itemPriceNote, it.price_change_24h)
            setPriceNoteText(itemPriceNote, it.price_change_percentage_24h)
            rank.text = "#${it.market_cap_rank}"
            marketCap.text = formatPrice(it.market_cap)
            circSupply.text = formatNumberInt(it.circulating_supply)
            totalSupply.text = formatNumberInt(it.total_supply)
            maxSupply.text = formatNumberInt(it.max_supply)
            ath.text = formatPrice(it.ath)
            inputCoin.hint = it.name
            inputCoin.setText("1")
            inputUsd.setText(formatNumber(it.current_price))

            binding.fab.setOnClickListener {
                openWebsite()
            }

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification() {
        var builder = Notification.Builder(activity, ItemDetailHostActivity().MOJ_KANAL)
            .setSmallIcon(android.R.drawable.star_on)
            .setLargeIcon(BitmapFactory.decodeResource(resources, android.R.drawable.btn_plus))
            .setContentTitle("Price alert")
            .setContentText("You will be notified of any major price changes")
            .setSubText("Never miss a thing")

        val resultIntent = Intent(activity, ItemDetailHostActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        builder.setContentIntent(pendingIntent)

        var notification: Notification = builder.build()
        var notificationManager: NotificationManager =
            activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // We are using switch case because multiple icons can be kept
        when (item.itemId) {
            R.id.action_share -> {
                val sharingIntent = Intent(Intent.ACTION_SEND)

                sharingIntent.type = "text/plain"

                val shareBody = "Share ${coin?.name} with your friends"
                val shareSubject = "Number go up"

                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject)

                startActivity(Intent.createChooser(sharingIntent, "Share via"))
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        super.onResume()
        mediaPlayer!!.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer!!.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
    }

}