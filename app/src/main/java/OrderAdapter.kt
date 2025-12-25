package com.example.carfixapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carfixapplication.api.Order
import com.example.carfixapplication.databinding.ItemOrderBinding

class OrdersAdapter(
    private var orders: List<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context
        val template = context.getString(R.string.order_list_item_format)

        val formattedData = formatApiDate(order.time)

        val info = String.format(template, order.id, formattedData, order.cost)

        holder.binding.orderInfoText.text = info


        // Устанавливаем обработчик клика
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }


    //сообщает RecyclerView, сколько всего элементов в списке.

    override fun getItemCount() = orders.size

    //вспомогательный метод для обновления данных в адаптере.

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged() // Эта команда обновляет отображаемый список
    }
}
