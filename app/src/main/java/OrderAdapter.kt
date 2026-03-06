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

        val formattedData = formatApiDate(order.time)

        holder.binding.carNumberTextView.text = "Номер: ${order.car_number}"

        holder.binding.orderDateTextView.text = "Дата: $formattedData"

        holder.binding.orderCostTextView.text = "Стоимость: ${order.cost} руб."

        holder.itemView.setOnClickListener { onItemClick(order) }
    }



    override fun getItemCount() = orders.size


    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}