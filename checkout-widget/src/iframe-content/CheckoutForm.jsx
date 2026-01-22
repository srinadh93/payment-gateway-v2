import React, { useEffect } from 'react';
import axios from 'axios';

const CheckoutForm = ({ orderId }) => {
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);

  useEffect(() => {
    // Listen for messages from parent window
    window.addEventListener('message', (event) => {
      // Handle close events from parent
      if (event.data.type === 'close_modal') {
        window.parent.postMessage({ type: 'close_modal' }, '*');
      }
    });
  }, []);

  const handlePayment = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post('/api/v1/payments', {
        order_id: orderId,
        method: 'upi',
        vpa: 'test@paytm'
      }, {
        headers: {
          'X-Api-Key': 'key_test_abc123',
          'X-Api-Secret': 'secret_test_xyz789'
        }
      });

      // Simulate payment processing
      setTimeout(() => {
        window.parent.postMessage({
          type: 'payment_success',
          data: response.data
        }, '*');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.error || 'Payment failed');
      window.parent.postMessage({
        type: 'payment_failed',
        data: { error: error }
      }, '*');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h2>Complete Payment</h2>
      {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}
      <form onSubmit={handlePayment}>
        <div style={{ marginBottom: '15px' }}>
          <label>
            Order ID:
            <input
              type="text"
              value={orderId}
              readOnly
              style={{
                width: '100%',
                padding: '8px',
                marginTop: '5px',
                boxSizing: 'border-box'
              }}
            />
          </label>
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label>
            Payment Method:
            <select
              defaultValue="upi"
              style={{
                width: '100%',
                padding: '8px',
                marginTop: '5px',
                boxSizing: 'border-box'
              }}
            >
              <option value="upi">UPI</option>
              <option value="card">Card</option>
            </select>
          </label>
        </div>
        <button
          type="submit"
          disabled={loading}
          style={{
            width: '100%',
            padding: '10px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: loading ? 'not-allowed' : 'pointer',
            opacity: loading ? 0.6 : 1
          }}
        >
          {loading ? 'Processing...' : 'Pay Now'}
        </button>
      </form>
    </div>
  );
};

export default CheckoutForm;
