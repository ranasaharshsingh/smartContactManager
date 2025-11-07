console.log("This is js")
// alert("main.js is addded!")

const toggleSidebar=()=>{


    if($('.sidebar').is(':visible')){

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }
    else{

        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");

    }
};
const search = () => {
    let query = document.getElementById("search-input").value;

    if (query == '') {
        $(".search-result").hide();
    } else {
        console.log(query);

        let url = `http://localhost:8010/user/search/${encodeURIComponent(query)}`;

        fetch(url)
            .then((response) => response.json())
            .then((data) => {
                let text = `<div class='list-group'>`;
                console.log("First contact:", data[0]);
                // ✅ Correct way: iterate directly on data (not data.array)
                data.forEach((contact) => {
                    text += `<a href='/user/show-contact/contact/${contact.customerId}' 
               class='list-group-item list-group-item-action'>
               ${contact.name}
             </a>`;
                });

                text += `</div>`;

                $(".search-result").html(text);
                $(".search-result").show();
            });
    }
};

// first request to server to create order
const paymentStart = () =>{
    console.log("Payment Started..");
    let amount=$("#payment_field").val();
    console.log(amount);
    if(amount=="" || amount==null)
    {
        swal("amount is required");
        return ;
    }
    if(amount<1){
        swal("Enter Valid Amount");
        return ;
    }
    
// we will use aJax to requeest server to create order
$.ajax(
    {
        url:"/user/create-order",
        data:JSON.stringify({amount:amount,info:'order_request'}),
        
        contentType:'application/json',
        type:'POST',
        dataType:'json',
        success:function(response){
            console.log(response)
            if(response.status == "created")
            {
                // open payment form
                let options={
                    key:"rzp_test_RBGjpTKqPP5Lmi",
                    amount:response.amount,
                    currency:'INR',
                    name:'Smart Contact MAnager',
                    description:"donation",
                    order_id:response.id,
                    handler:function(response){
                        console.log(response.razorpay_payment_id);
                         console.log(response.razorpay_order_id);
                          console.log(response.razorpay_signature);
                          console.log("Payment Successfull !!");

                        updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");

                          

                    },
                    "prefill": {
                    "name": "",
                    "email": "",
                    "contact": ""
                    },
                    "notes": {
                    "address": "Harsh Solanki"

                    },
                    "theme": {
                    "color": "#3399cc"
                    }
                };
                var rzp1 = new Razorpay(options);
                rzp1.on('payment.failed', function (response){
                console.log(response.error.code);
                console.log(response.error.description);
                console.log(response.error.source);
                console.log(response.error.step);
                console.log(response.error.reason);
                console.log(response.error.metadata.order_id);
                console.log(response.error.metadata.payment_id);
                swal("OOPs!! Payment Failed !!","","error");
                });
                rzp1.open();
            }
        },
        error:function(error){
            console.log(error)
            swal("something went wrong","","error");
        }
    }
)
};

function updatePaymentOnServer(payment_id,order_id,status)
{
    $.ajax(
        {
         url:"/user/update-order",
        data:JSON.stringify({payment_id:payment_id,
            order_id:order_id,
            status:status}),
        
        contentType:'application/json',
        type:'POST',
        dataType:'json',
        success:function(response)
        {
                swal("congrats !! payment success!!", "", "success");
        },
        error:function(error){
            swal("Payment success but can't update in server!! Please wait we will contact you soon!!","","error");
        }
        }
    );
}
