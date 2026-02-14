import { render, screen } from '@testing-library/react';
import App from './App';

test('renders job portal heading', () => {
  render(<App />);
  const headingElement = screen.getByText(/job portal/i);
  expect(headingElement).toBeInTheDocument();
});
