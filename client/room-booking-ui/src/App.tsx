import { Toaster } from "react-hot-toast";
import BookingPage from "./pages/BookingPage";

function App() {
  return (
    <>
      <BookingPage />
      <Toaster position="top-right" />
    </>
  );
}

export default App;
