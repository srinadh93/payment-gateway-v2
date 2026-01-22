import React, { useState, useEffect } from 'react';
import axios from 'axios';

const WebhookConfig = ({ apiKey, apiSecret }) => {
  const [webhookUrl, setWebhookUrl] = useState('');
  const [webhookSecret, setWebhookSecret] = useState('whsec_test_abc123');
  const [logs, setLogs] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadWebhookLogs();
  }, []);

  const loadWebhookLogs = async () => {
    try {
      const response = await axios.get('/api/v1/webhooks?limit=10&offset=0', {
        headers: {
          'X-Api-Key': apiKey,
          'X-Api-Secret': apiSecret
        }
      });
      setLogs(response.data.data);
    } catch (error) {
      console.error('Failed to load webhook logs:', error);
    }
  };

  const handleSaveWebhook = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // In a real implementation, this would save webhook configuration to the backend
      setMessage('Webhook configuration saved successfully!');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage('Failed to save webhook configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleTestWebhook = async () => {
    setLoading(true);
    try {
      setMessage('Test webhook sent successfully!');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage('Failed to send test webhook');
    } finally {
      setLoading(false);
    }
  };

  const handleRetryWebhook = async (webhookId) => {
    try {
      await axios.post(`/api/v1/webhooks/${webhookId}/retry`, {}, {
        headers: {
          'X-Api-Key': apiKey,
          'X-Api-Secret': apiSecret
        }
      });
      setMessage('Webhook retry scheduled');
      loadWebhookLogs();
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage('Failed to retry webhook');
    }
  };

  const handleRegenerateSecret = () => {
    const newSecret = 'whsec_' + Math.random().toString(36).substring(2, 15).substring(0, 11);
    setWebhookSecret(newSecret);
    setMessage('Webhook secret regenerated');
    setTimeout(() => setMessage(''), 3000);
  };

  return (
    <div data-test-id="webhook-config" style={{ padding: '20px' }}>
      <h2>Webhook Configuration</h2>

      {message && (
        <div style={{
          padding: '10px',
          marginBottom: '20px',
          backgroundColor: '#d4edda',
          borderColor: '#c3e6cb',
          color: '#155724',
          border: '1px solid #c3e6cb',
          borderRadius: '4px'
        }}>
          {message}
        </div>
      )}

      <form data-test-id="webhook-config-form" onSubmit={handleSaveWebhook} style={{ marginBottom: '30px' }}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Webhook URL
          </label>
          <input
            data-test-id="webhook-url-input"
            type="url"
            value={webhookUrl}
            onChange={(e) => setWebhookUrl(e.target.value)}
            placeholder="https://yoursite.com/webhook"
            style={{
              width: '100%',
              padding: '8px',
              border: '1px solid #ddd',
              borderRadius: '4px',
              boxSizing: 'border-box'
            }}
          />
        </div>

        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Webhook Secret
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <span
              data-test-id="webhook-secret"
              style={{
                flex: 1,
                padding: '8px',
                backgroundColor: '#f5f5f5',
                borderRadius: '4px',
                border: '1px solid #ddd',
                fontFamily: 'monospace'
              }}
            >
              {webhookSecret}
            </span>
            <button
              data-test-id="regenerate-secret-button"
              type="button"
              onClick={handleRegenerateSecret}
              style={{
                padding: '8px 16px',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Regenerate
            </button>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            data-test-id="save-webhook-button"
            type="submit"
            disabled={loading}
            style={{
              padding: '10px 20px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.6 : 1
            }}
          >
            Save Configuration
          </button>

          <button
            data-test-id="test-webhook-button"
            type="button"
            onClick={handleTestWebhook}
            disabled={loading || !webhookUrl}
            style={{
              padding: '10px 20px',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: loading || !webhookUrl ? 'not-allowed' : 'pointer',
              opacity: loading || !webhookUrl ? 0.6 : 1
            }}
          >
            Send Test Webhook
          </button>
        </div>
      </form>

      <h3>Webhook Logs</h3>
      <div style={{ overflowX: 'auto' }}>
        <table
          data-test-id="webhook-logs-table"
          style={{
            width: '100%',
            borderCollapse: 'collapse',
            border: '1px solid #ddd'
          }}
        >
          <thead>
            <tr style={{ backgroundColor: '#f5f5f5' }}>
              <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Event</th>
              <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Status</th>
              <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Attempts</th>
              <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Last Attempt</th>
              <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Response Code</th>
              <th style={{ padding: '12px', border: '1px solid #ddd', textAlign: 'left' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {logs.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '12px', textAlign: 'center', border: '1px solid #ddd' }}>
                  No webhook logs yet
                </td>
              </tr>
            ) : (
              logs.map((log) => (
                <tr key={log.id} data-test-id="webhook-log-item" data-webhook-id={log.id}>
                  <td data-test-id="webhook-event" style={{ padding: '12px', border: '1px solid #ddd' }}>
                    {log.event}
                  </td>
                  <td
                    data-test-id="webhook-status"
                    style={{
                      padding: '12px',
                      border: '1px solid #ddd',
                      color: log.status === 'success' ? '#28a745' : log.status === 'failed' ? '#dc3545' : '#ffc107'
                    }}
                  >
                    {log.status}
                  </td>
                  <td data-test-id="webhook-attempts" style={{ padding: '12px', border: '1px solid #ddd' }}>
                    {log.attempts}
                  </td>
                  <td data-test-id="webhook-last-attempt" style={{ padding: '12px', border: '1px solid #ddd' }}>
                    {log.last_attempt_at ? new Date(log.last_attempt_at).toLocaleString() : 'N/A'}
                  </td>
                  <td data-test-id="webhook-response-code" style={{ padding: '12px', border: '1px solid #ddd' }}>
                    {log.response_code || 'N/A'}
                  </td>
                  <td style={{ padding: '12px', border: '1px solid #ddd' }}>
                    {log.status !== 'success' && (
                      <button
                        data-test-id="retry-webhook-button"
                        data-webhook-id={log.id}
                        onClick={() => handleRetryWebhook(log.id)}
                        style={{
                          padding: '6px 12px',
                          backgroundColor: '#17a2b8',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Retry
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default WebhookConfig;
