import { useState, useRef, useEffect } from "react";
import { Bot, Send, Loader2, Bug } from "lucide-react";
import { Layout } from "../../components/layout";
import apiClient from "../../api/client";

interface ChatMessage {
  id: string;
  sender: "user" | "bot";
  text: string;
  timestamp: string;
  cardJson?: string;
  commandType?: string;
}

interface SimulateResponse {
  message: string;
  cardJson: string;
  commandType: string;
  success: boolean;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

const quickCommands = [
  { label: "Check Availability", text: "check availability today from 10am to 12pm" },
  { label: "My Bookings", text: "my bookings" },
  { label: "Help", text: "help" },
  { label: "Book Room", text: "book Conference Room A today from 10am to 11am" },
];

function formatTimestamp(): string {
  return new Date().toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function renderCard(cardJson: string | undefined): React.ReactNode {
  if (!cardJson) return null;
  try {
    const card = JSON.parse(cardJson);
    const body = card.body || [];

    return (
      <div className="mt-2 bg-white border border-gray-200 rounded-lg p-3 shadow-sm max-w-md">
        {body.map((element: { type: string; text?: string; color?: string; weight?: string; size?: string; isSubtle?: boolean; columns?: Array<{ items?: Array<{ type: string; text?: string; weight?: string; isSubtle?: boolean }> }> }, idx: number) => {
          if (element.type === "TextBlock") {
            const isAttention = element.color === "Attention";
            const isWarning = element.color === "Warning";
            const isAccent = element.color === "Accent";
            const isBold = element.weight === "Bolder" || element.size === "Large";

            return (
              <p
                key={idx}
                className={`text-sm mb-1 ${
                  isAttention ? "text-red-600 font-medium" : ""
                } ${isWarning ? "text-amber-600" : ""} ${
                  isAccent ? "text-blue-600" : ""
                } ${isBold ? "font-bold text-base" : "text-gray-700"} ${
                  element.isSubtle ? "text-gray-500" : ""
                }`}
              >
                {element.text}
              </p>
            );
          }

          if (element.type === "ColumnSet" && element.columns) {
            const items = element.columns.flatMap((col) => col.items || []);
            return (
              <div key={idx} className="border-t border-gray-100 pt-2 mt-2">
                {items.map((item, i) => {
                  if (item.type === "TextBlock") {
                    return (
                      <p
                        key={i}
                        className={`text-sm mb-0.5 ${
                          item.weight === "Bolder" ? "font-semibold text-gray-800" : "text-gray-500"
                        }`}
                      >
                        {item.text}
                      </p>
                    );
                  }
                  if (item.type === "ActionSet") {
                    return null;
                  }
                  return null;
                })}
              </div>
            );
          }

          return null;
        })}
      </div>
    );
  } catch {
    return null;
  }
}

export default function BotSimulator() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "welcome",
      sender: "bot",
      text: "Welcome to Room Booking Bot Simulator! Type 'help' to see available commands.",
      timestamp: formatTimestamp(),
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [lastDebug, setLastDebug] = useState<{
    response: SimulateResponse;
    request: { text: string; userEmail: string };
  } | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const sendMessage = async (text: string) => {
    if (!text.trim() || loading) return;

    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: "user",
      text,
      timestamp: formatTimestamp(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setLoading(true);

    try {
      const apiResponse = (await apiClient.post("/messages/simulate", {
        text,
        userEmail: "dev@company.com",
      })) as unknown as ApiResponse<SimulateResponse>;

      const response = apiResponse.data;

      const botMsg: ChatMessage = {
        id: `bot-${Date.now()}`,
        sender: "bot",
        text: response.message || "",
        timestamp: formatTimestamp(),
        cardJson: response.cardJson || undefined,
        commandType: response.commandType,
      };

      setMessages((prev) => [...prev, botMsg]);
      setLastDebug({
        response,
        request: { text, userEmail: "dev@company.com" },
      });
    } catch (err) {
      const errorMsg: ChatMessage = {
        id: `error-${Date.now()}`,
        sender: "bot",
        text: `Error: ${String(err)}`,
        timestamp: formatTimestamp(),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    sendMessage(input);
  };

  return (
    <Layout title="Bot Simulator">
      <div className="flex gap-4 h-[calc(100vh-120px)]">
        {/* Chat Window - 70% */}
        <div className="flex flex-col bg-white rounded-lg shadow flex-[7] overflow-hidden">
          {/* Header */}
          <div className="flex items-center gap-2 px-4 py-3 bg-gray-900 text-white">
            <Bot className="w-5 h-5 text-blue-400" />
            <span className="font-semibold">Room Booking Bot (Simulator)</span>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex ${
                  msg.sender === "user" ? "justify-end" : "justify-start"
                }`}
              >
                <div
                  className={`max-w-[70%] ${
                    msg.sender === "user"
                      ? "bg-blue-600 text-white rounded-lg rounded-br-none px-4 py-2"
                      : "bg-gray-200 text-gray-800 rounded-lg rounded-bl-none px-4 py-2"
                  }`}
                >
                  {msg.text && <p className="text-sm whitespace-pre-wrap">{msg.text}</p>}
                  {msg.sender === "bot" && renderCard(msg.cardJson)}
                  <span
                    className={`text-xs mt-1 block ${
                      msg.sender === "user" ? "text-blue-200" : "text-gray-400"
                    }`}
                  >
                    {msg.timestamp}
                  </span>
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-gray-200 rounded-lg rounded-bl-none px-4 py-2">
                  <Loader2 className="w-4 h-4 text-gray-500 animate-spin" />
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick Commands */}
          <div className="flex gap-2 px-4 py-2 bg-white border-t border-gray-200">
            {quickCommands.map((cmd) => (
              <button
                key={cmd.label}
                onClick={() => sendMessage(cmd.text)}
                disabled={loading}
                className="px-3 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 rounded-full hover:bg-blue-100 transition-colors disabled:opacity-50"
              >
                {cmd.label}
              </button>
            ))}
          </div>

          {/* Input */}
          <form onSubmit={handleSubmit} className="flex gap-2 p-4 bg-white border-t border-gray-200">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your message..."
              disabled={loading}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              className="flex items-center gap-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors disabled:opacity-50"
            >
              <Send className="w-4 h-4" />
              Send
            </button>
          </form>
        </div>

        {/* Debug Panel - 30% */}
        <div className="flex-[3] bg-gray-900 rounded-lg shadow overflow-hidden flex flex-col">
          <div className="flex items-center gap-2 px-4 py-3 border-b border-gray-700">
            <Bug className="w-4 h-4 text-green-400" />
            <span className="text-sm font-semibold text-white">Debug Panel</span>
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {lastDebug ? (
              <>
                <div>
                  <h4 className="text-xs font-semibold text-gray-400 uppercase mb-1">Request</h4>
                  <pre className="text-xs text-green-400 bg-gray-800 rounded p-2 overflow-x-auto">
                    {JSON.stringify(lastDebug.request, null, 2)}
                  </pre>
                </div>

                <div>
                  <h4 className="text-xs font-semibold text-gray-400 uppercase mb-1">Command Type</h4>
                  <div className="text-sm text-blue-400 font-mono">
                    {lastDebug.response.commandType}
                  </div>
                </div>

                <div>
                  <h4 className="text-xs font-semibold text-gray-400 uppercase mb-1">Message</h4>
                  <div className="text-sm text-white">
                    {lastDebug.response.message || "(card response)"}
                  </div>
                </div>

                <div>
                  <h4 className="text-xs font-semibold text-gray-400 uppercase mb-1">Card JSON</h4>
                  <pre className="text-xs text-gray-300 bg-gray-800 rounded p-2 overflow-x-auto max-h-96">
                    {lastDebug.response.cardJson
                      ? JSON.stringify(JSON.parse(lastDebug.response.cardJson), null, 2)
                      : "null"}
                  </pre>
                </div>
              </>
            ) : (
              <div className="text-sm text-gray-500 text-center mt-8">
                Send a message to see debug info
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}
