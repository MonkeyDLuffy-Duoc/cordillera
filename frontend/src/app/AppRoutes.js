import React, { Component, Suspense, lazy } from 'react'
import { Switch, Route, Redirect } from 'react-router-dom'

const Dashboard = lazy(() => import('./dashboard/Dashboard'))
const Signin = lazy(() => import('./general-pages/Signin'))
const Signup = lazy(() => import('./general-pages/Signup'))

const Buttons = lazy(() => import('./ui-elements/Buttons'))
const Dropdowns = lazy(() => import('./ui-elements/Dropdowns'))
const Icons = lazy(() => import('./ui-elements/Icons'))
const FormElements = lazy(() => import('./form/FormElements'))
const ChartJs = lazy(() => import('./charts/ChartJs'))
const BasicTable = lazy(() => import('./tables/BasicTable'))

// Route Guard Component
const PrivateRoute = ({ component: Component, ...rest }) => (
  <Route 
    {...rest} 
    render={(props) => (
      localStorage.getItem('token')
        ? <Component {...props} />
        : <Redirect to='/general-pages/signin' />
    )} 
  />
)

export class AppRoutes extends Component {
  render() {
    return (
      <Suspense fallback=''>
        <Switch>
          <Route exact path="/">
            <Redirect to="/dashboard"></Redirect>
          </Route>
          
          {/* Protected routes */}
          <PrivateRoute exact path="/dashboard" component={ Dashboard } />
          <PrivateRoute exact path="/ui-elements/buttons" component={ Buttons } />
          <PrivateRoute exact path="/ui-elements/dropdowns" component={ Dropdowns } />
          <PrivateRoute exact path="/ui-elements/icons" component={ Icons } />
          <PrivateRoute exact path="/form/form-elements" component={ FormElements } />
          <PrivateRoute exact path="/charts/chartjs" component={ ChartJs } />
          <PrivateRoute exact path="/tables/basic-table" component={ BasicTable } />

          {/* Public routes */}
          <Route exact path="/general-pages/signin" component={ Signin } />
          <Route exact path="/general-pages/signup" component={ Signup } />
          
          <Route path="*">
            <Redirect to="/dashboard" />
          </Route>
        </Switch>
      </Suspense>
    )
  }
}

export default AppRoutes
